import React, { useState, useEffect } from 'react';
import { MissionControlHeader } from '../components/MissionControlHeader';
import { SpaceEnvironmentPanel } from '../components/SpaceEnvironmentPanel';
import { RiskAssessmentPanel } from '../components/RiskAssessmentPanel';
import { SatelliteFleetPanel } from '../components/SatelliteFleetPanel';
import { DecisionSupportPanel } from '../components/DecisionSupportPanel';
import { HistoricalTelemetryCharts } from '../components/HistoricalTelemetryCharts';
import { EventHorizonTimeline } from '../components/EventHorizonTimeline';
import { AlertsTicker } from '../components/AlertsTicker';
import { MissionControlMap } from '../components/MissionControlMap';
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
    const defaultSats: Satellite[] = [
      { satelliteId: 'SAT-001-ISS', name: 'ISS / Zarya Module', missionType: 'Manned Low Earth Orbit Laboratory', orbitType: 'LEO', altitudeKm: 420.0, inclinationDeg: 51.6, latitude: -28.5, longitude: 45.2, healthStatus: 'NOMINAL', radiationSensitivity: 'CRITICAL', communicationSensitivity: 'HIGH', navigationSensitivity: 'HIGH', operationalStatus: 'ACTIVE', createdAt: '', updatedAt: '' },
      { satelliteId: 'SAT-002-STARLINK', name: 'Starlink Constellation Leader', missionType: 'Broadband Mega-Constellation', orbitType: 'LEO', altitudeKm: 550.0, inclinationDeg: 53.2, latitude: 35.1, longitude: -80.4, healthStatus: 'NOMINAL', radiationSensitivity: 'HIGH', communicationSensitivity: 'CRITICAL', navigationSensitivity: 'MEDIUM', operationalStatus: 'ACTIVE', createdAt: '', updatedAt: '' },
      { satelliteId: 'SAT-003-GOES16', name: 'GOES-16 East Space Environment', missionType: 'Geostationary Solar & Weather Observatory', orbitType: 'GEO', altitudeKm: 35786.0, inclinationDeg: 0.1, latitude: 0.0, longitude: -75.2, healthStatus: 'NOMINAL', radiationSensitivity: 'LOW', communicationSensitivity: 'CRITICAL', navigationSensitivity: 'LOW', operationalStatus: 'ACTIVE', createdAt: '', updatedAt: '' },
      { satelliteId: 'SAT-004-GPS3', name: 'GPS-III Navstar SV04', missionType: 'Global Positioning and Timing Constellation', orbitType: 'MEO', altitudeKm: 20200.0, inclinationDeg: 55.0, latitude: 12.8, longitude: 125.4, healthStatus: 'NOMINAL', radiationSensitivity: 'MEDIUM', communicationSensitivity: 'HIGH', navigationSensitivity: 'CRITICAL', operationalStatus: 'ACTIVE', createdAt: '', updatedAt: '' },
    ];

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
          setSatellites(dashData.satellites && dashData.satellites.length > 0 ? dashData.satellites : (satsData.length > 0 ? satsData : defaultSats));
        } else {
          setSatellites(satsData.length > 0 ? satsData : defaultSats);
        }

        if (eventsData && eventsData.length > 0) {
          setRecentEvents(eventsData);
          if (!dashData?.latestEvent) setSelectedEvent(eventsData[0]);
        }
      } catch (err) {
        console.error('Failed to load initial space telemetry:', err);
        setSatellites(defaultSats);
      } finally {
        setLoading(false);
      }
    };

    const safetyTimer = setTimeout(() => {
      setLoading(false);
      setSatellites((prev) => (prev.length > 0 ? prev : defaultSats));
    }, 2000);

    loadData().finally(() => clearTimeout(safetyTimer));

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

  const handleExecuteAll = async () => {
    const pending = recommendations.filter((r) => r.status === 'PENDING');
    // Optimistically mark all recommendations as EXECUTED
    setRecommendations((prev) =>
      prev.map((r) => ({ ...r, status: 'EXECUTED' }))
    );
    // Restore all satellites back to NOMINAL operational status
    setSatellites((prev) =>
      prev.map((s) => ({
        ...s,
        healthStatus: 'NOMINAL',
        operationalStatus: 'ACTIVE',
      }))
    );

    try {
      // Dispatches execution for pending recommendations
      for (const rec of pending.slice(0, 15)) {
        apiService.updateRecommendationStatus(rec.recommendationId, 'EXECUTED').catch(() => {});
      }
    } catch (err) {
      console.error('Failed to execute all directives:', err);
    }
  };

  const handleAcknowledgeAlert = async (alertId: string) => {
    // Optimistically mark acknowledged immediately
    setAlerts((prev) =>
      prev.map((a) => (a.alertId === alertId ? { ...a, acknowledged: true } : a))
    );
    try {
      await apiService.acknowledgeAlert(alertId);
    } catch (err) {
      console.error('Failed to acknowledge alert on server:', err);
    }
  };

  const handleAcknowledgeAll = async () => {
    const unacknowledged = alerts.filter((a) => !a.acknowledged);
    // Optimistically mark all acknowledged immediately
    setAlerts((prev) => prev.map((a) => ({ ...a, acknowledged: true })));

    try {
      for (const alt of unacknowledged.slice(0, 10)) {
        apiService.acknowledgeAlert(alt.alertId).catch(() => {});
      }
    } catch (err) {
      console.error('Failed to acknowledge all alerts:', err);
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
    <div className="min-h-screen w-full flex flex-col bg-space-950 text-slate-100 scanline overflow-y-auto select-none">
      {/* Top Header */}
      <MissionControlHeader
        summary={summary}
        wsConnected={wsConnected}
        onOpenSimulator={onOpenSimulator}
      />

      {/* Active Alerts Banner */}
      <AlertsTicker
        alerts={alerts}
        onAcknowledgeAlert={handleAcknowledgeAlert}
        onAcknowledgeAll={handleAcknowledgeAll}
      />

      {/* Main Operations Dashboard Container */}
      <div className="flex-1 flex flex-col space-y-4 p-4">
        {/* Tier 1: Space Weather Environment (Left) & Geospatial Radar Hero (Right, Wide) */}
        <div className="grid grid-cols-12 gap-4">
          {/* Top Left: Space Weather Environment (4 cols) */}
          <div className="col-span-12 lg:col-span-4 flex flex-col">
            <SpaceEnvironmentPanel event={selectedEvent} />
          </div>

          {/* Top Right: Geospatial Radar & Event Horizon Scrubber (8 cols) */}
          <div className="col-span-12 lg:col-span-8 flex flex-col space-y-3">
            <div className="rounded-lg overflow-hidden relative flex-1">
              <MissionControlMap
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
        </div>

        {/* Tier 2: 3-Way Split Section (Fleet, Risk Assessment, Directives) */}
        <div className="grid grid-cols-12 gap-4">
          {/* Middle Left: Virtual Satellite Fleet (4 cols) */}
          <div className="col-span-12 lg:col-span-4 flex flex-col">
            <SatelliteFleetPanel
              satellites={satellites}
              selectedSatelliteId={selectedSatellite?.satelliteId}
              onSelectSatellite={(sat) => setSelectedSatellite(sat)}
            />
          </div>

          {/* Middle Center: Hybrid Risk Assessment (4 cols) */}
          <div className="col-span-12 lg:col-span-4 flex flex-col">
            <RiskAssessmentPanel
              assessment={activeAssessment}
              satelliteCount={satellites.length}
            />
          </div>

          {/* Middle Right: Operational Decision Directives (4 cols) */}
          <div className="col-span-12 lg:col-span-4 flex flex-col">
            <DecisionSupportPanel
              recommendations={recommendations}
              onExecuteAction={handleExecuteAction}
              onExecuteAll={handleExecuteAll}
            />
          </div>
        </div>

        {/* Tier 3: Bottom Historical Telemetry Graphs & Log Table */}
        <div className="pb-4 shrink-0">
          <HistoricalTelemetryCharts
            events={recentEvents}
            riskAssessments={riskAssessments}
          />
        </div>
      </div>
    </div>
  );
};

