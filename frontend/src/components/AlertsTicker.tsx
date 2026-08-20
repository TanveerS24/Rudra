import React from 'react';
import { AlertTriangle, Check, BellRing, CheckCheck } from 'lucide-react';
import { Alert } from '../types';

interface AlertsTickerProps {
  alerts: Alert[];
  onAcknowledgeAlert?: (alertId: string) => void;
  onAcknowledgeAll?: () => void;
}

export const AlertsTicker: React.FC<AlertsTickerProps> = ({
  alerts,
  onAcknowledgeAlert,
  onAcknowledgeAll,
}) => {
  const active = alerts.filter((a) => !a.acknowledged);
  if (active.length === 0) return null;

  const topAlert = active[0];
  const isCritical = topAlert.severity === 'CRITICAL';

  return (
    <div
      className={`px-4 py-2 flex items-center justify-between border-b text-xs font-mono select-none transition-all duration-200 ${
        isCritical
          ? 'bg-red-950/90 border-red-500/70 text-red-200 shadow-glow-red'
          : 'bg-amber-950/90 border-amber-500/70 text-amber-200 shadow-glow-amber'
      }`}
    >
      <div className="flex items-center space-x-3 overflow-hidden">
        <BellRing className={`w-4 h-4 shrink-0 ${isCritical ? 'text-red-400 animate-pulse' : 'text-amber-400'}`} />
        <div className="truncate">
          <strong className="tracking-wide text-white">
            [{topAlert.severity} ALERT {active.length > 1 ? `(1 of ${active.length})` : ''}] {topAlert.title}:
          </strong>{' '}
          <span className="text-slate-200 font-sans">{topAlert.message}</span>
        </div>
      </div>

      <div className="flex items-center space-x-2 shrink-0 ml-3">
        {active.length > 1 && (
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onAcknowledgeAll?.();
            }}
            className="flex items-center space-x-1 px-3 py-1 rounded text-[11px] font-bold font-mono transition-all cursor-pointer bg-red-700/80 hover:bg-red-600 text-white border border-red-400 shadow-md"
            title="Acknowledge and dismiss all active alerts"
          >
            <CheckCheck className="w-3.5 h-3.5" />
            <span>ACKNOWLEDGE ALL ({active.length})</span>
          </button>
        )}

        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onAcknowledgeAlert?.(topAlert.alertId);
          }}
          className={`flex items-center space-x-1 px-3 py-1 rounded text-[11px] font-bold font-mono transition-all cursor-pointer ${
            isCritical
              ? 'bg-red-600 hover:bg-red-500 text-white shadow-md'
              : 'bg-amber-600 hover:bg-amber-500 text-space-950 shadow-md'
          }`}
        >
          <Check className="w-3.5 h-3.5" />
          <span>ACKNOWLEDGE</span>
        </button>
      </div>
    </div>
  );
};

