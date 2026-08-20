import React from 'react';
import { Sliders, Activity } from 'lucide-react';
import { DashboardSummary } from '../types';

interface HeaderProps {
  summary: DashboardSummary | null;
  wsConnected: boolean;
  onOpenSimulator: () => void;
}

export const MissionControlHeader: React.FC<HeaderProps> = ({
  wsConnected,
  onOpenSimulator,
}) => {
  return (
    <header className="h-14 bg-space-950/80 border-b border-blue-900/40 px-6 flex items-center justify-between backdrop-blur-md z-30 shrink-0 select-none relative">
      {/* Left Minimal Status Indicator */}
      <div className="flex items-center space-x-2 w-48">
        <div className="flex items-center space-x-2 bg-space-900/60 border border-blue-900/40 px-2.5 py-1 rounded-full text-[11px] font-mono">
          <span className={`w-2 h-2 rounded-full ${wsConnected ? 'bg-emerald-400 animate-pulse shadow-[0_0_8px_#34d399]' : 'bg-amber-400'}`} />
          <span className="text-slate-300">{wsConnected ? 'LIVE FEED' : 'CONNECTING'}</span>
        </div>
      </div>

      {/* Centered Project Title */}
      <div className="flex-1 flex flex-col items-center justify-center text-center">
        <div className="flex items-center space-x-2">
          <Activity className="w-4 h-4 text-hud-cyan animate-pulse" />
          <h1 className="text-sm md:text-base font-bold tracking-widest uppercase font-mono">
            <span className="text-hud-cyan font-black">RUDRA</span>
            <span className="text-slate-500 mx-2">/</span>
            <span className="bg-gradient-to-r from-slate-100 via-slate-200 to-hud-cyan bg-clip-text text-transparent">
              SPACE WEATHER OPERATIONS
            </span>
          </h1>
        </div>
        <p className="text-[9px] text-slate-400 font-mono tracking-widest uppercase">
          Autonomous Satellite Fleet Resilience & Decision Matrix
        </p>
      </div>

      {/* Right Controls */}
      <div className="flex items-center justify-end space-x-3 w-48">
        <button
          onClick={onOpenSimulator}
          className="flex items-center space-x-1.5 px-3 py-1.5 rounded-md bg-blue-600/20 hover:bg-blue-600/40 border border-hud-cyan/40 hover:border-hud-cyan text-hud-cyan text-xs font-mono transition-all duration-150 shadow-glow-cyan"
        >
          <Sliders className="w-3.5 h-3.5" />
          <span className="font-semibold">SIMULATOR DECK</span>
        </button>
      </div>
    </header>
  );
};

