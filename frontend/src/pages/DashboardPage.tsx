import React, { useState, useEffect } from 'react';
import { MissionControlHeader } from '../components/MissionControlHeader';
import { SpaceEnvironmentPanel } from '../components/SpaceEnvironmentPanel';
import { RiskAssessmentPanel } from '../components/RiskAssessmentPanel';
import { SatelliteFleetPanel } from '../components/SatelliteFleetPanel';
import { DecisionSupportPanel } from '../components/DecisionSupportPanel';
import { HistoricalTelemetryCharts } from '../components/HistoricalTelemetryCharts';
import { EventHorizonTimeline } from '../components/EventHorizonTimeline';
import { AlertsTicker } from '../components/AlertsTicker';
import { GlobeScene } from '../globe/GlobeScene';
import { apiService } from '../services/api';
import { wsClient } from '../services/websocket';
import {
  DashboardSummary,
  SpaceWeatherEvent,
  Satellite,
  RiskAssessment,
  Recommendation,
  Alert,
} from '../types';

interface DashboardProps {
  onOpenSimulator: () => void;
}

export const DashboardPage: React.FC<DashboardProps> = ({ onOpenSimulator }) => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [selectedEvent, setSelectedEvent] = useState<SpaceWeatherEvent | null>(null);
  const [selectedSatellite, setSelectedSatellite] = useState<Satellite | null>(null);
  const [recentEvents, setRecentEvents] = useState<SpaceWeatherEvent[]>([]);
  const [riskAssessments, setRiskAssessments] = useState<RiskAssessment[]>([]);
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [satellites, setSatellites] = useState<Satellite[]>([]);
  const [wsConnected, setWsConnected] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);

  // Initial Data Fetch
  useEffect(() => {
    const loadData = async () => {
      try {
        const [dashData, eventsData, satsData] = await Promise.all([
          apiService.getDashboardSummary().catch(() => null),
          apiService.getRecentEvents(20).catch(() => []),
          apiService.getAllSatellites().catch(() => []),
        ]);

        if (dashData) {
          setSummary(dashData);
          setSelectedEvent(dashData.latestEvent);
          setRiskAssessments(dashData.activeRiskAssessments || []);
          setRecommendations(dashData.pendingRecommendations || []);
          setAlerts(dashData.activeAlerts || []);
          setSatellites(dashData.satellites || satsData);
        } else {
          setSatellites(satsData);
        }

        if (eventsData && eventsData.length > 0) {
          setRecentEvents(eventsData);
          if (!dashData?.latestEvent) setSelectedEvent(eventsData[0]);
        }
      } catch (err) {
        console.error('Failed to load initial space telemetry:', err);
      } finally {
        setLoading(false);
      }
    };

    loadData();

    // Connect WebSocket
    wsClient.connect();
    const interval = setInterval(() => {
      setWsConnected(wsClient.getStatus());
    }, 1000);

    // Subscribe to WebSocket events
    const unsubscribe = wsClient.subscribe((msg) => {
      console.log('[WS EVENT RECEIVED]', msg.type);
      if (msg.type === 'SPACE_WEATHER_EVENT') {
        const newEvt = msg.payload as SpaceWeatherEvent;
        setSelectedEvent(newEvt);
        setRecentEvents((prev) => [newEvt, ...prev.filter((e) => e.eventId !== newEvt.eventId)]);
      } else if (msg.type === 'RISK_UPDATED') {
        const newRisks = msg.payload as RiskAssessment[];
        setRiskAssessments(newRisks);
      } else if (msg.type === 'RECOMMENDATION_CREATED') {
        const newRecs = msg.payload as Recommendation[];
        setRecommendations((prev) => [...newRecs, ...prev]);
      } else if (msg.type === 'ALERT_CREATED') {
        const newAlert = msg.payload as Alert;
        setAlerts((prev) => [newAlert, ...prev]);
      } else if (msg.type === 'SATELLITE_STATUS_CHANGED') {
        const newSats = msg.payload as Satellite[];
        setSatellites(newSats);
      }
    });

    return () => {
      clearInterval(interval);
      unsubscribe();
    };
  }, []);

  const handleExecuteAction = async (recId: string) => {
    try {
      await apiService.updateRecommendationStatus(recId, 'EXECUTED');
      setRecommendations((prev) =>
        prev.map((r) => (r.recommendationId === recId ? { ...r, status: 'EXECUTED' } : r))
      );
    } catch (err) {
      console.error('Failed to execute recommendation:', err);
    }
  };

  const handleAcknowledgeAlert = async (alertId: string) => {
    try {
      await apiService.acknowledgeAlert(alertId);
      setAlerts((prev) =>
        prev.map((a) => (a.alertId === alertId ? { ...a, acknowledged: true } : a))
      );
    } catch (err) {
      console.error('Failed to acknowledge alert:', err);
    }
  };

  if (loading) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-space-950 text-hud-cyan font-mono space-y-4">
        <div className="w-12 h-12 border-2 border-hud-cyan border-t-transparent rounded-full animate-spin shadow-glow-cyan" />
        <div className="text-sm tracking-widest animate-pulse">
          INITIALIZING SPACE WEATHER TELEMETRY MATRIX...
        </div>
      </div>
    );
  }

  const activeAssessment =
    riskAssessments.find((r) => r.eventId === selectedEvent?.eventId) ||
    summary?.highestRiskAssessment ||
    (riskAssessments.length > 0 ? riskAssessments[0] : null);

  return (
    <div className="h-screen w-screen flex flex-col bg-space-950 text-slate-100 scanline overflow-hidden select-none">
      {/* Top Header */}
      <MissionControlHeader
        summary={summary}
        wsConnected={wsConnected}
        onOpenSimulator={onOpenSimulator}
      />

      {/* Active Alerts Banner */}
      <AlertsTicker alerts={alerts} onAcknowledgeAlert={handleAcknowledgeAlert} />

      {/* Main Grid View */}
      <div className="flex-1 grid grid-cols-12 gap-3 p-3 overflow-hidden">
        {/* Left Column: Space Weather & Constellation Fleet (3 cols) */}
        <div className="col-span-12 lg:col-span-3 flex flex-col space-y-3 overflow-hidden">
          <SpaceEnvironmentPanel event={selectedEvent} />
          <div className="flex-1 overflow-hidden">
            <SatelliteFleetPanel
              satellites={satellites}
              selectedSatelliteId={selectedSatellite?.satelliteId}
              onSelectSatellite={(sat) => setSelectedSatellite(sat)}
            />
          </div>
        </div>

        {/* Center Column: 3D Earth Globe & Event Horizon (6 cols) */}
        <div className="col-span-12 lg:col-span-6 flex flex-col space-y-3 overflow-hidden">
          {/* 3D Earth Centerpiece */}
          <div className="flex-1 hud-panel rounded-lg overflow-hidden relative min-h-[360px]">
            <GlobeScene
              impactLocation={selectedEvent?.maximumImpactLocation}
              intensity={selectedEvent?.intensity}
              geomagneticIndex={selectedEvent?.geomagneticIndex}
              satellites={satellites}
              selectedSatelliteId={selectedSatellite?.satelliteId}
              onSelectSatellite={(sat) => setSelectedSatellite(sat)}
            />
          </div>

          {/* Event Horizon Timeline Scrubber */}
          <EventHorizonTimeline
            events={recentEvents}
            selectedEventId={selectedEvent?.eventId}
            onSelectEvent={(evt) => setSelectedEvent(evt)}
          />
        </div>

        {/* Right Column: Hybrid Risk & Decision Directives (3 cols) */}
        <div className="col-span-12 lg:col-span-3 flex flex-col space-y-3 overflow-hidden">
          <RiskAssessmentPanel
            assessment={activeAssessment}
            satelliteCount={satellites.length}
          />
          <DecisionSupportPanel
            recommendations={recommendations}
            onExecuteAction={handleExecuteAction}
          />
        </div>
      </div>

      {/* Bottom Collapsible / Historical Telemetry Graphs */}
      <div className="px-3 pb-3 shrink-0">
        <HistoricalTelemetryCharts
          events={recentEvents}
          riskAssessments={riskAssessments}
        />
      </div>
    </div>
  );
};
