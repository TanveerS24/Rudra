import React, { useState } from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  LineChart,
  Line,
} from 'recharts';
import { BarChart3, TrendingUp } from 'lucide-react';
import { SpaceWeatherEvent, RiskAssessment } from '../types';

interface ChartsProps {
  events: SpaceWeatherEvent[];
  riskAssessments: RiskAssessment[];
}

export const HistoricalTelemetryCharts: React.FC<ChartsProps> = ({
  events,
  riskAssessments,
}) => {
  const [metricTab, setMetricTab] = useState<'ENVIRONMENT' | 'RISK'>('ENVIRONMENT');

  // Format real event data for Recharts
  const chartData = events.slice(0, 15).reverse().map((e, idx) => {
    const timeLabel = new Date(e.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const matchingRisk = riskAssessments.find((r) => r.eventId === e.eventId);

    return {
      time: timeLabel || `T-${15 - idx}`,
      solarWind: Math.round(e.solarWindSpeed),
      geomagneticKp: e.geomagneticIndex,
      riskScore: matchingRisk ? Math.round(matchingRisk.finalScore) : (e.geomagneticIndex * 9 + 15),
      intensity: e.intensity,
    };
  });

  return (
    <div className="hud-panel p-4 rounded-lg flex flex-col space-y-3">
      {/* Header & Tabs */}
      <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
        <div className="flex items-center space-x-2 text-hud-cyan">
          <BarChart3 className="w-4 h-4" />
          <h2 className="text-xs font-bold font-mono tracking-wider uppercase">
            Live Telemetry & Historical Trends
          </h2>
        </div>

        <div className="flex items-center space-x-1 font-mono text-[10px]">
          <button
            onClick={() => setMetricTab('ENVIRONMENT')}
            className={`px-2.5 py-1 rounded border transition-colors ${metricTab === 'ENVIRONMENT' ? 'bg-hud-cyan/20 border-hud-cyan text-hud-cyan font-bold' : 'bg-space-950/40 border-slate-800 text-slate-400'}`}
          >
            SOLAR WIND & Kp
          </button>
          <button
            onClick={() => setMetricTab('RISK')}
            className={`px-2.5 py-1 rounded border transition-colors ${metricTab === 'RISK' ? 'bg-hud-amber/20 border-hud-amber text-hud-amber font-bold' : 'bg-space-950/40 border-slate-800 text-slate-400'}`}
          >
            RISK TIMELINE
          </button>
        </div>
      </div>

      {/* Chart Canvas */}
      <div className="h-44 w-full">
        <ResponsiveContainer width="100%" height="100%">
          {metricTab === 'ENVIRONMENT' ? (
            <AreaChart data={chartData}>
              <defs>
                <linearGradient id="colorWind" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#00f0ff" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#00f0ff" stopOpacity={0.0} />
                </linearGradient>
                <linearGradient id="colorKp" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.5} />
                  <stop offset="95%" stopColor="#f59e0b" stopOpacity={0.0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(59, 130, 246, 0.1)" />
              <XAxis dataKey="time" stroke="#64748b" tick={{ fontSize: 10, fontFamily: 'monospace' }} />
              <YAxis yAxisId="wind" stroke="#00f0ff" domain={[300, 1000]} tick={{ fontSize: 10, fontFamily: 'monospace' }} />
              <YAxis yAxisId="kp" orientation="right" stroke="#f59e0b" domain={[0, 9]} tick={{ fontSize: 10, fontFamily: 'monospace' }} />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#060d1d',
                  borderColor: '#1e3a8a',
                  fontFamily: 'monospace',
                  fontSize: '11px',
                }}
              />
              <Area yAxisId="wind" type="monotone" dataKey="solarWind" stroke="#00f0ff" fillOpacity={1} fill="url(#colorWind)" name="Solar Wind (km/s)" />
              <Area yAxisId="kp" type="monotone" dataKey="geomagneticKp" stroke="#f59e0b" fillOpacity={1} fill="url(#colorKp)" name="Planetary Kp" />
            </AreaChart>
          ) : (
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(59, 130, 246, 0.1)" />
              <XAxis dataKey="time" stroke="#64748b" tick={{ fontSize: 10, fontFamily: 'monospace' }} />
              <YAxis domain={[0, 100]} stroke="#ef4444" tick={{ fontSize: 10, fontFamily: 'monospace' }} />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#060d1d',
                  borderColor: '#ef4444',
                  fontFamily: 'monospace',
                  fontSize: '11px',
                }}
              />
              <Line type="monotone" dataKey="riskScore" stroke="#ef4444" strokeWidth={2.5} dot={{ fill: '#ef4444', r: 3 }} name="Risk Score (0-100)" />
            </LineChart>
          )}
        </ResponsiveContainer>
      </div>
    </div>
  );
};
