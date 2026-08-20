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
import { BarChart3, ListOrdered, ShieldAlert } from 'lucide-react';
import { SpaceWeatherEvent, RiskAssessment } from '../types';

interface ChartsProps {
  events: SpaceWeatherEvent[];
  riskAssessments: RiskAssessment[];
}

export const HistoricalTelemetryCharts: React.FC<ChartsProps> = ({
  events,
  riskAssessments,
}) => {
  const [metricTab, setMetricTab] = useState<'ENVIRONMENT' | 'RISK' | 'LOG'>('ENVIRONMENT');

  // Format latest 20 real event data for Recharts
  const latest20 = events.slice(0, 20);
  const chartData = [...latest20].reverse().map((e, idx) => {
    const timeLabel = new Date(e.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const matchingRisk = riskAssessments.find((r) => r.eventId === e.eventId);

    return {
      time: timeLabel || `T-${20 - idx}`,
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
            Historical Space Telemetry & Event Trends ({latest20.length} Records)
          </h2>
        </div>

        <div className="flex items-center space-x-1 font-mono text-[10px]">
          <button
            onClick={() => setMetricTab('ENVIRONMENT')}
            className={`px-2.5 py-1 rounded border transition-colors ${metricTab === 'ENVIRONMENT' ? 'bg-hud-cyan/20 border-hud-cyan text-hud-cyan font-bold' : 'bg-space-950/40 border-slate-800 text-slate-400 hover:text-slate-200'}`}
          >
            SOLAR WIND & Kp
          </button>
          <button
            onClick={() => setMetricTab('RISK')}
            className={`px-2.5 py-1 rounded border transition-colors ${metricTab === 'RISK' ? 'bg-hud-amber/20 border-hud-amber text-hud-amber font-bold' : 'bg-space-950/40 border-slate-800 text-slate-400 hover:text-slate-200'}`}
          >
            RISK TIMELINE
          </button>
          <button
            onClick={() => setMetricTab('LOG')}
            className={`px-2.5 py-1 rounded border transition-colors flex items-center space-x-1 ${metricTab === 'LOG' ? 'bg-blue-600/30 border-blue-400 text-hud-cyan font-bold' : 'bg-space-950/40 border-slate-800 text-slate-400 hover:text-slate-200'}`}
          >
            <ListOrdered className="w-3 h-3" />
            <span>LATEST 20 EVENTS LOG</span>
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      {metricTab === 'LOG' ? (
        <div className="overflow-x-auto max-h-56 overflow-y-auto">
          <table className="w-full text-left font-mono text-[11px] text-slate-300">
            <thead className="bg-space-900/90 text-[10px] text-hud-cyan uppercase border-b border-blue-900/50 sticky top-0">
              <tr>
                <th className="py-2 px-3">EVENT ID</th>
                <th className="py-2 px-3">TYPE</th>
                <th className="py-2 px-3">INTENSITY</th>
                <th className="py-2 px-3">SOLAR WIND</th>
                <th className="py-2 px-3">Kp INDEX</th>
                <th className="py-2 px-3">IMPACT COORDINATES</th>
                <th className="py-2 px-3">TIMESTAMP</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {latest20.map((evt) => {
                const isX = evt.intensity.toUpperCase().startsWith('X');
                const isM = evt.intensity.toUpperCase().startsWith('M');
                const badgeColor = isX ? 'text-red-400 bg-red-950/60 border-red-500/50' : isM ? 'text-amber-400 bg-amber-950/60 border-amber-500/50' : 'text-cyan-400 bg-cyan-950/60 border-cyan-500/50';

                return (
                  <tr key={evt.eventId} className="hover:bg-blue-950/40 transition-colors">
                    <td className="py-1.5 px-3 font-bold text-slate-200">{evt.eventId}</td>
                    <td className="py-1.5 px-3">
                      <span className="text-slate-300">{evt.eventType.replace(/_/g, ' ')}</span>
                    </td>
                    <td className="py-1.5 px-3">
                      <span className={`px-1.5 py-0.5 rounded border text-[10px] font-bold ${badgeColor}`}>
                        {evt.intensity}
                      </span>
                    </td>
                    <td className="py-1.5 px-3 text-hud-cyan font-semibold">{Math.round(evt.solarWindSpeed)} km/s</td>
                    <td className="py-1.5 px-3 text-orange-400 font-bold">Kp {evt.geomagneticIndex}</td>
                    <td className="py-1.5 px-3 text-slate-400">
                      {evt.maximumImpactLocation.latitude.toFixed(1)}°N, {evt.maximumImpactLocation.longitude.toFixed(1)}°E
                    </td>
                    <td className="py-1.5 px-3 text-slate-500 text-[10px]">
                      {new Date(evt.timestamp).toLocaleTimeString()}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      ) : (
        /* Chart Canvas */
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
      )}
    </div>
  );
};

