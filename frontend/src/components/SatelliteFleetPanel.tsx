import React, { useState } from 'react';
import { Satellite as SatelliteIcon, Shield, Radio, Navigation, Eye } from 'lucide-react';
import { Satellite } from '../types';

interface FleetPanelProps {
  satellites: Satellite[];
  selectedSatelliteId?: string | null;
  onSelectSatellite: (sat: Satellite) => void;
}

export const SatelliteFleetPanel: React.FC<FleetPanelProps> = ({
  satellites,
  selectedSatelliteId,
  onSelectSatellite,
}) => {
  const [filterOrbit, setFilterOrbit] = useState<string>('ALL');

  const filtered = satellites.filter((s) => filterOrbit === 'ALL' || s.orbitType === filterOrbit);

  return (
    <div className="hud-panel p-4 rounded-lg flex flex-col space-y-3 h-full">
      {/* Header & Filter Controls */}
      <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
        <div className="flex items-center space-x-2 text-hud-cyan">
          <SatelliteIcon className="w-4 h-4" />
          <h2 className="text-xs font-bold font-mono tracking-wider uppercase">
            Virtual Satellite Fleet ({satellites.length})
          </h2>
        </div>

        {/* Orbit Filter Tabs */}
        <div className="flex items-center space-x-1 font-mono text-[10px]">
          {['ALL', 'LEO', 'MEO', 'GEO'].map((orbit) => (
            <button
              key={orbit}
              onClick={() => setFilterOrbit(orbit)}
              className={`px-2 py-0.5 rounded border transition-colors ${filterOrbit === orbit ? 'bg-hud-cyan/20 border-hud-cyan text-hud-cyan font-bold' : 'bg-space-950/40 border-slate-800 text-slate-400 hover:text-slate-200'}`}
            >
              {orbit}
            </button>
          ))}
        </div>
      </div>

      {/* Satellite Fleet Cards List */}
      <div className="space-y-2 overflow-y-auto max-h-[340px] pr-1">
        {filtered.map((sat) => {
          const isSelected = selectedSatelliteId === sat.satelliteId;
          const isSafeMode = sat.operationalStatus === 'SAFE_MODE' || sat.healthStatus === 'DEGRADED';

          const healthBadge =
            sat.healthStatus === 'CRITICAL' ? 'text-red-400 bg-red-950/50 border-red-500/60' :
            isSafeMode ? 'text-amber-400 bg-amber-950/50 border-amber-500/60 animate-pulse' :
            'text-emerald-400 bg-emerald-950/50 border-emerald-500/60';

          return (
            <div
              key={sat.satelliteId}
              onClick={() => onSelectSatellite(sat)}
              className={`p-2.5 rounded border transition-all cursor-pointer ${isSelected ? 'bg-blue-950/60 border-hud-cyan shadow-glow-cyan' : 'bg-space-900/70 border-blue-900/30 hover:border-blue-700/60'}`}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <span className="text-xs font-bold font-mono text-slate-100">{sat.name}</span>
                  <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-blue-900/40 text-hud-cyan border border-blue-800/40">
                    {sat.orbitType}
                  </span>
                </div>
                <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${healthBadge}`}>
                  {sat.operationalStatus === 'SAFE_MODE' ? 'SAFE MODE' : sat.healthStatus}
                </span>
              </div>

              {/* Sub-metrics */}
              <div className="grid grid-cols-3 gap-2 mt-2 pt-2 border-t border-slate-800/60 text-[10px] font-mono text-slate-400">
                <div>
                  <span className="text-slate-500">ALTITUDE:</span>{' '}
                  <span className="text-slate-200">{Math.round(sat.altitudeKm)} km</span>
                </div>

                <div className="flex items-center space-x-1">
                  <Shield className="w-2.5 h-2.5 text-hud-purple" />
                  <span>RAD: <strong className="text-slate-200">{sat.radiationSensitivity}</strong></span>
                </div>

                <div className="flex items-center space-x-1">
                  <Radio className="w-2.5 h-2.5 text-hud-cyan" />
                  <span>COMM: <strong className="text-slate-200">{sat.communicationSensitivity}</strong></span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
