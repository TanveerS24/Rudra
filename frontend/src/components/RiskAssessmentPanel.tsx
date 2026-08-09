import React from 'react';
import { ShieldAlert, AlertTriangle, CheckCircle2, ChevronRight } from 'lucide-react';
import { RiskAssessment } from '../types';

interface RiskPanelProps {
  assessment: RiskAssessment | null;
  satelliteCount?: number;
}

export const RiskAssessmentPanel: React.FC<RiskPanelProps> = ({
  assessment,
  satelliteCount = 8,
}) => {
  const score = assessment?.finalScore ?? 42.0;
  const riskLevel = assessment?.riskLevel ?? 'MODERATE';
  const factors = assessment?.primaryFactors || [
    'Moderate solar wind dynamic pressure',
    'Planetary Kp index elevating geomagnetic activity',
    'Polar LEO orbit crossing sensitivity profile'
  ];
  const effects = assessment?.potentialEffects || [
    'Elevated upper-atmospheric drag on Low Earth Orbit constellations',
    'Single Event Upset (SEU) risk on radiation-sensitive memory banks',
    'L-band GPS pseudo-range timing carrier jitter'
  ];

  const levelColor =
    riskLevel === 'CRITICAL' ? 'text-red-400 border-red-500/60 bg-red-950/40 shadow-glow-red' :
    riskLevel === 'HIGH' ? 'text-orange-400 border-orange-500/60 bg-orange-950/40 shadow-glow-amber' :
    riskLevel === 'MODERATE' ? 'text-amber-400 border-amber-500/60 bg-amber-950/40' :
    'text-emerald-400 border-emerald-500/60 bg-emerald-950/30';

  // Circular gauge calculations
  const radius = 38;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (score / 100) * circumference;

  return (
    <div className="hud-panel p-4 rounded-lg flex flex-col space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
        <div className="flex items-center space-x-2 text-hud-amber">
          <ShieldAlert className="w-4 h-4" />
          <h2 className="text-xs font-bold font-mono tracking-wider uppercase">
            Hybrid Risk Assessment
          </h2>
        </div>
        <span className="text-[10px] font-mono text-slate-400">
          ASSESS ID: {assessment?.assessmentId || 'RISK-DEF-1'}
        </span>
      </div>

      {/* Score Dial & Risk Status Summary */}
      <div className="flex items-center space-x-4 p-3 rounded bg-space-900/80 border border-blue-900/40">
        {/* SVG Circular Progress Gauge */}
        <div className="relative w-24 h-24 flex items-center justify-center shrink-0">
          <svg className="w-full h-full transform -rotate-90" viewBox="0 0 96 96">
            <circle
              cx="48"
              cy="48"
              r={radius}
              stroke="rgba(30, 41, 59, 0.8)"
              strokeWidth="8"
              fill="transparent"
            />
            <circle
              cx="48"
              cy="48"
              r={radius}
              stroke={score >= 76 ? '#ef4444' : score >= 51 ? '#f97316' : score >= 26 ? '#f59e0b' : '#10b981'}
              strokeWidth="8"
              strokeDasharray={circumference}
              strokeDashoffset={strokeDashoffset}
              strokeLinecap="round"
              fill="transparent"
              className="transition-all duration-700 ease-out"
            />
          </svg>
          <div className="absolute flex flex-col items-center justify-center text-center">
            <span className="text-xl font-bold font-mono text-slate-100 telemetry-val">
              {Math.round(score)}
            </span>
            <span className="text-[9px] text-slate-400 font-mono -mt-1">/ 100</span>
          </div>
        </div>

        {/* Risk Classification Readout */}
        <div className="flex-1 space-y-2">
          <div className={`px-3 py-1 rounded border inline-flex items-center space-x-1.5 font-mono font-bold text-xs ${levelColor}`}>
            <AlertTriangle className="w-3.5 h-3.5" />
            <span>RISK LEVEL: {riskLevel}</span>
          </div>

          <p className="text-[11px] text-slate-300 leading-snug">
            Deterministic physics evaluation + LLM contextual reasoning across{' '}
            <strong className="text-hud-cyan">{satelliteCount} active satellites</strong>.
          </p>
        </div>
      </div>

      {/* Primary Physical Causal Factors */}
      <div className="space-y-1.5">
        <div className="text-[10px] font-mono text-hud-cyan tracking-wider uppercase flex items-center space-x-1">
          <ChevronRight className="w-3 h-3" />
          <span>Primary Physical Risk Factors</span>
        </div>
        <ul className="space-y-1 text-xs font-sans text-slate-300">
          {factors.map((f, i) => (
            <li key={i} className="flex items-start space-x-2 bg-space-950/40 p-2 rounded border border-slate-800/80">
              <span className="w-1.5 h-1.5 rounded-full bg-hud-amber mt-1.5 shrink-0" />
              <span>{f}</span>
            </li>
          ))}
        </ul>
      </div>

      {/* Potential Satellite Operational Consequences */}
      <div className="space-y-1.5">
        <div className="text-[10px] font-mono text-orange-400 tracking-wider uppercase flex items-center space-x-1">
          <ChevronRight className="w-3 h-3" />
          <span>Potential Subsystem Impacts</span>
        </div>
        <ul className="space-y-1 text-xs font-sans text-slate-300">
          {effects.map((e, i) => (
            <li key={i} className="flex items-start space-x-2 bg-space-950/40 p-2 rounded border border-slate-800/80">
              <span className="w-1.5 h-1.5 rounded-full bg-hud-red mt-1.5 shrink-0" />
              <span>{e}</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};
