import React from 'react';
import { Sun, Wind, Compass, Zap, MapPin, Gauge } from 'lucide-react';
import { SpaceWeatherEvent } from '../types';

interface EnvironmentPanelProps {
  event: SpaceWeatherEvent | null;
}

export const SpaceEnvironmentPanel: React.FC<EnvironmentPanelProps> = ({ event }) => {
  const intensity = event?.intensity || 'M1.5';
  const solarWind = event?.solarWindSpeed || 450.0;
  const kp = event?.geomagneticIndex ?? 3;
  const radiation = event?.radiationLevel || 'NORMAL';
  const eventType = event?.eventType?.replace('_', ' ') || 'SOLAR FLARE';

  const isCritical = intensity.startsWith('X') || kp >= 8;
  const isHigh = intensity.startsWith('M') || kp >= 6;

  const kpColor =
    kp >= 8 ? 'text-red-400 bg-red-950/40 border-red-500/60' :
    kp >= 6 ? 'text-orange-400 bg-orange-950/40 border-orange-500/60' :
    kp >= 4 ? 'text-amber-400 bg-amber-950/40 border-amber-500/60' :
    'text-emerald-400 bg-emerald-950/40 border-emerald-500/60';

  const radColor =
    radiation === 'CRITICAL' ? 'text-red-400 border-red-500/50 bg-red-950/30' :
    radiation === 'HIGH' ? 'text-orange-400 border-orange-500/50 bg-orange-950/30' :
    radiation === 'ELEVATED' ? 'text-amber-400 border-amber-500/50 bg-amber-950/30' :
    'text-cyan-400 border-cyan-500/50 bg-cyan-950/30';

  return (
    <div className="hud-panel p-4 rounded-lg flex flex-col justify-between h-full space-y-4 min-h-[460px]">
      {/* Panel Header */}
      <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
        <div className="flex items-center space-x-2 text-hud-cyan">
          <Sun className="w-4 h-4 animate-spin-slow" />
          <h2 className="text-xs font-bold font-mono tracking-wider uppercase">
            Space Weather Environment
          </h2>
        </div>
        <span className="text-[10px] font-mono text-slate-400">
          ID: {event?.eventId || 'EVT-ACTIVE-1'}
        </span>
      </div>

      {/* Main Metric Cards Grid */}
      <div className="grid grid-cols-2 gap-2.5">
        {/* Solar Flare / Event Class */}
        <div className="p-2.5 rounded bg-space-900/80 border border-blue-900/50">
          <div className="flex items-center justify-between text-[10px] text-slate-400 font-mono mb-1">
            <span>EVENT TYPE</span>
            <Zap className="w-3 h-3 text-hud-amber" />
          </div>
          <div className="text-lg font-bold font-mono text-hud-amber telemetry-val">
            {intensity}
          </div>
          <div className="text-[10px] text-slate-300 font-mono uppercase truncate">
            {eventType}
          </div>
        </div>

        {/* Planetary Kp Geomagnetic Index */}
        <div className="p-2.5 rounded bg-space-900/80 border border-blue-900/50">
          <div className="flex items-center justify-between text-[10px] text-slate-400 font-mono mb-1">
            <span>PLANETARY Kp</span>
            <Gauge className="w-3 h-3 text-hud-cyan" />
          </div>
          <div className="flex items-baseline space-x-1.5">
            <span className={`text-lg font-bold font-mono px-2 py-0.5 rounded border ${kpColor}`}>
              {kp}.0
            </span>
            <span className="text-[10px] text-slate-400 font-mono">/ 9.0</span>
          </div>
          <div className="text-[10px] text-slate-400 font-mono mt-1">
            {kp >= 7 ? 'SEVERE STORM (G3+)' : kp >= 5 ? 'MODERATE ACTIVITY' : 'QUIET / NOMINAL'}
          </div>
        </div>

        {/* Solar Wind Velocity */}
        <div className="p-2.5 rounded bg-space-900/80 border border-blue-900/50">
          <div className="flex items-center justify-between text-[10px] text-slate-400 font-mono mb-1">
            <span>SOLAR WIND</span>
            <Wind className="w-3 h-3 text-hud-blue" />
          </div>
          <div className="text-lg font-bold font-mono text-blue-300 telemetry-val">
            {Math.round(solarWind)} <span className="text-xs font-normal text-slate-400">km/s</span>
          </div>
          {/* Progress bar */}
          <div className="w-full bg-slate-800 h-1 rounded mt-1.5 overflow-hidden">
            <div
              className="bg-hud-cyan h-full rounded transition-all duration-500"
              style={{ width: `${Math.min(100, (solarWind / 1000) * 100)}%` }}
            />
          </div>
        </div>

        {/* Radiation Flux Level */}
        <div className="p-2.5 rounded bg-space-900/80 border border-blue-900/50">
          <div className="flex items-center justify-between text-[10px] text-slate-400 font-mono mb-1">
            <span>RADIATION FLUX</span>
            <Compass className="w-3 h-3 text-hud-purple" />
          </div>
          <div className={`text-sm font-bold font-mono px-2 py-1 rounded border inline-block ${radColor}`}>
            {radiation}
          </div>
          <div className="text-[10px] text-slate-400 font-mono mt-1">
            CONF: {event ? Math.round(event.confidence * 100) : 92}%
          </div>
        </div>
      </div>

      {/* Origin & Impact Coordinates HUD */}
      <div className="p-2.5 rounded bg-space-950/60 border border-blue-900/40 text-xs font-mono space-y-1.5">
        <div className="flex items-center justify-between text-slate-300">
          <span className="flex items-center space-x-1.5 text-slate-400 text-[10px]">
            <MapPin className="w-3 h-3 text-hud-cyan" />
            <span>TERRESTRIAL IMPACT:</span>
          </span>
          <span className="text-hud-cyan font-bold">
            {event ? `${event.maximumImpactLocation.latitude.toFixed(1)}°, ${event.maximumImpactLocation.longitude.toFixed(1)}°` : '28.5°, -80.6°'}
          </span>
        </div>

        <div className="flex items-center justify-between text-slate-300">
          <span className="flex items-center space-x-1.5 text-slate-400 text-[10px]">
            <Sun className="w-3 h-3 text-hud-amber" />
            <span>SOLAR ORIGIN:</span>
          </span>
          <span className="text-hud-amber">
            {event ? `${event.origin.latitude.toFixed(1)}°, ${event.origin.longitude.toFixed(1)}°` : '14.2°, 45.1°'}
          </span>
        </div>
      </div>

      {/* Description readout */}
      {event?.impactDescription && (
        <div className="text-[11px] text-slate-300 font-sans leading-relaxed bg-space-950/40 p-2 rounded border border-slate-800">
          {event.impactDescription}
        </div>
      )}
    </div>
  );
};
