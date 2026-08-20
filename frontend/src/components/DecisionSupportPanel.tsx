import React from 'react';
import { Target, CheckCircle, Clock, ShieldCheck, ArrowRight, CheckCheck } from 'lucide-react';
import { Recommendation } from '../types';

interface DecisionPanelProps {
  recommendations: Recommendation[];
  onExecuteAction?: (recommendationId: string) => void;
  onExecuteAll?: () => void;
}

export const DecisionSupportPanel: React.FC<DecisionPanelProps> = ({
  recommendations,
  onExecuteAction,
  onExecuteAll,
}) => {
  const pending = recommendations.filter((r) => r.status === 'PENDING');
  // Limit to top 10 most critical / recent pending directives to keep DOM lightweight and focused
  const displayList = pending.length > 0 ? pending.slice(0, 10) : recommendations.slice(0, 3);

  return (
    <div className="hud-panel p-4 rounded-lg flex flex-col space-y-3">
      {/* Header with Execute All Action */}
      <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
        <div className="flex items-center space-x-2 text-hud-emerald">
          <Target className="w-4 h-4" />
          <h2 className="text-xs font-bold font-mono tracking-wider uppercase">
            Operational Directives ({pending.length > 0 ? pending.length.toLocaleString() : '0'})
          </h2>
        </div>

        {pending.length > 0 ? (
          <button
            type="button"
            onClick={onExecuteAll}
            className="flex items-center space-x-1 px-2.5 py-1 rounded bg-emerald-600/30 hover:bg-emerald-600/50 border border-emerald-500/60 text-emerald-300 text-[10px] font-mono font-bold transition-all shadow-glow-emerald"
            title="Execute and approve all pending operational recommendations"
          >
            <CheckCheck className="w-3 h-3" />
            <span>EXECUTE ALL</span>
          </button>
        ) : (
          <span className="text-[10px] font-mono text-slate-400">
            ALL EXECUTED
          </span>
        )}
      </div>

      {/* Recommendations Cards List */}
      <div className="space-y-3 max-h-[380px] overflow-y-auto pr-1">
        {displayList.length === 0 ? (
          <div className="text-center py-6 text-xs text-slate-400 font-mono">
            No pending operational directives. All constellations nominal.
          </div>
        ) : (
          displayList.map((rec) => (
            <div
              key={rec.recommendationId}
              className="p-3 rounded bg-space-900/80 border border-blue-900/40 space-y-2 relative overflow-hidden"
            >
              {/* Action Title & Confidence */}
              <div className="flex items-start justify-between">
                <div className="flex items-start space-x-2">
                  <span className="w-2 h-2 rounded-full bg-hud-emerald mt-1 shrink-0 animate-ping-slow" />
                  <h3 className="text-xs font-bold font-sans text-slate-100 leading-snug">
                    {rec.action}
                  </h3>
                </div>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-950/60 text-emerald-400 border border-emerald-800/50 shrink-0 ml-2">
                  CONF: {Math.round(rec.confidence * 100)}%
                </span>
              </div>

              {/* Why / Reasoning */}
              <div className="text-[11px] text-slate-300 font-sans leading-relaxed bg-space-950/60 p-2 rounded border border-slate-800">
                <span className="text-hud-cyan font-semibold font-mono text-[10px] block mb-0.5">
                  OPERATIONAL JUSTIFICATION:
                </span>
                {rec.reasoning}
              </div>

              {/* Expected Impact */}
              {rec.expectedImpact && (
                <div className="text-[11px] text-slate-300 font-sans flex items-start space-x-1.5 pt-1">
                  <ShieldCheck className="w-3.5 h-3.5 text-hud-emerald mt-0.5 shrink-0" />
                  <span>
                    <strong className="text-slate-200">EXPECTED OUTCOME:</strong> {rec.expectedImpact}
                  </span>
                </div>
              )}

              {/* Execute / Acknowledge Action Button */}
              <div className="pt-2 flex items-center justify-between border-t border-slate-800/60 text-xs font-mono">
                <span className="text-[10px] text-slate-400">
                  STATUS: <strong className="text-hud-amber">{rec.status}</strong>
                </span>

                {rec.status === 'PENDING' && (
                  <button
                    onClick={() => onExecuteAction?.(rec.recommendationId)}
                    className="flex items-center space-x-1 px-3 py-1 rounded bg-emerald-600/30 hover:bg-emerald-600/50 border border-emerald-500/50 text-emerald-300 text-xs font-mono font-bold transition-all shadow-glow-emerald"
                  >
                    <span>EXECUTE DIRECTIVE</span>
                    <ArrowRight className="w-3 h-3" />
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
