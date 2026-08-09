import React, { useState, useEffect } from 'react';
import { Activity, Radio, Cpu, Database, ShieldAlert, Sparkles, Sliders } from 'lucide-react';
import { DashboardSummary } from '../types';

interface HeaderProps {
  summary: DashboardSummary | null;
  wsConnected: boolean;
  onOpenSimulator: () => void;
}

export const MissionControlHeader: React.FC<HeaderProps> = ({
  summary,
  wsConnected,
  onOpenSimulator,
}) => {
  const [utcTime, setUtcTime] = useState<string>('');

  useEffect(() => {
    const update = () => {
      const now = new Date();
      setUtcTime(now.toUTCString().replace('GMT', 'ZULU'));
    };
    update();
    const interval = setInterval(update, 1000);
    return () => clearInterval(interval);
  }, []);

  const highestRisk = summary?.highestRiskAssessment?.riskLevel || 'LOW';
  const riskColor =
    highestRisk === 'CRITICAL' ? 'text-red-400 border-red-500/50 bg-red-950/40 shadow-glow-red animate-pulse' :
    highestRisk === 'HIGH' ? 'text-orange-400 border-orange-500/50 bg-orange-950/40 shadow-glow-amber' :
    highestRisk === 'MODERATE' ? 'text-amber-400 border-amber-500/50 bg-amber-950/40' :
    'text-hud-cyan border-cyan-500/40 bg-cyan-950/30';

  return (
    <header className="h-14 bg-space-900/90 border-b border-blue-900/40 px-4 flex items-center justify-between backdrop-blur-md z-30 shrink-0 select-none">
      {/* Title & Branding */}
      <div className="flex items-center space-x-3">
        <div className="w-8 h-8 rounded border border-hud-cyan/40 bg-cyan-950/60 flex items-center justify-center text-hud-cyan shadow-glow-cyan">
          <Activity className="w-5 h-5 animate-pulse" />
        </div>
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-sm font-bold tracking-wider text-slate-100 uppercase">
              Integrated Space Weather Operations
            </h1>
            <span className="text-[10px] px-1.5 py-0.5 rounded bg-blue-900/60 text-hud-cyan font-mono border border-blue-700/50">
              V1.0-LIVE
            </span>
          </div>
          <p className="text-[10px] text-slate-400 font-mono tracking-tight">
            DECISION SUPPORT & SATELLITE FLEET RESILIENCE MATRIX
          </p>
        </div>
      </div>

      {/* Subsystem Health Indicators */}
      <div className="hidden md:flex items-center space-x-4 text-xs font-mono">
        <div className="flex items-center space-x-1.5 text-emerald-400 bg-space-950/60 px-2.5 py-1 rounded border border-emerald-900/50">
          <Radio className="w-3.5 h-3.5 animate-pulse" />
          <span>SYS ONLINE</span>
        </div>

        <div className="flex items-center space-x-1.5 text-blue-400 bg-space-950/60 px-2.5 py-1 rounded border border-blue-900/50">
          <Cpu className="w-3.5 h-3.5" />
          <span>LLM: {summary?.systemHealth?.ollamaLLM || 'ONLINE'}</span>
        </div>

        <div className="flex items-center space-x-1.5 text-cyan-400 bg-space-950/60 px-2.5 py-1 rounded border border-cyan-900/50">
          <Database className="w-3.5 h-3.5" />
          <span>DB: {summary?.systemHealth?.database || 'CONNECTED'}</span>
        </div>

        <div className={`flex items-center space-x-1.5 px-2.5 py-1 rounded border ${wsConnected ? 'text-emerald-400 border-emerald-900/50 bg-emerald-950/20' : 'text-amber-400 border-amber-900/50 bg-amber-950/20'}`}>
          <span className={`w-2 h-2 rounded-full ${wsConnected ? 'bg-emerald-400 animate-ping-slow' : 'bg-amber-400'}`} />
          <span>WS: {wsConnected ? 'STREAM LIVE' : 'RECONNECTING'}</span>
        </div>
      </div>

      {/* Threat Level & Controls */}
      <div className="flex items-center space-x-3">
        {/* Threat Level Badge */}
        <div className={`flex items-center space-x-2 px-3 py-1 rounded border font-mono font-bold text-xs ${riskColor}`}>
          <ShieldAlert className="w-4 h-4" />
          <span>FLEET THREAT: {highestRisk}</span>
        </div>

        {/* Zulu Clock */}
        <div className="hidden lg:block text-right font-mono">
          <div className="text-xs text-slate-200 font-semibold">{utcTime}</div>
          <div className="text-[10px] text-hud-cyan/80">MISSION CLOCK</div>
        </div>

        {/* Simulator Button */}
        <button
          onClick={onOpenSimulator}
          className="flex items-center space-x-1.5 px-3 py-1.5 rounded bg-blue-600/30 hover:bg-blue-600/50 border border-hud-cyan/40 text-hud-cyan text-xs font-mono transition-all duration-150 shadow-glow-cyan"
        >
          <Sliders className="w-3.5 h-3.5" />
          <span className="font-semibold">SIMULATOR DECK</span>
        </button>
      </div>
    </header>
  );
};
