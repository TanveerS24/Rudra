import React from 'react';
import { AlertTriangle, Check, BellRing } from 'lucide-react';
import { Alert } from '../types';

interface AlertsTickerProps {
  alerts: Alert[];
  onAcknowledgeAlert?: (alertId: string) => void;
}

export const AlertsTicker: React.FC<AlertsTickerProps> = ({
  alerts,
  onAcknowledgeAlert,
}) => {
  const active = alerts.filter((a) => !a.acknowledged);
  if (active.length === 0) return null;

  const topAlert = active[0];
  const isCritical = topAlert.severity === 'CRITICAL';

  return (
    <div
      className={`px-4 py-2 flex items-center justify-between border-b text-xs font-mono select-none ${isCritical ? 'bg-red-950/80 border-red-500/60 text-red-200 animate-pulse' : 'bg-amber-950/80 border-amber-500/60 text-amber-200'}`}
    >
      <div className="flex items-center space-x-3">
        <BellRing className="w-4 h-4 text-hud-red shrink-0" />
        <div>
          <strong className="tracking-wide">[{topAlert.severity} ALERT] {topAlert.title}:</strong>{' '}
          <span className="text-slate-300 font-sans">{topAlert.message}</span>
        </div>
      </div>

      <button
        onClick={() => onAcknowledgeAlert?.(topAlert.alertId)}
        className="flex items-center space-x-1 px-2.5 py-1 rounded bg-red-900/50 hover:bg-red-900/80 border border-red-500/60 text-red-200 text-[10px] font-bold transition-colors"
      >
        <Check className="w-3 h-3" />
        <span>ACKNOWLEDGE</span>
      </button>
    </div>
  );
};
