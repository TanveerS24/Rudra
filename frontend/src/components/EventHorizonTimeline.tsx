import React from 'react';
import { Clock, Zap, AlertCircle } from 'lucide-react';
import { SpaceWeatherEvent } from '../types';

interface TimelineProps {
  events: SpaceWeatherEvent[];
  selectedEventId?: string | null;
  onSelectEvent: (event: SpaceWeatherEvent) => void;
}

export const EventHorizonTimeline: React.FC<TimelineProps> = ({
  events,
  selectedEventId,
  onSelectEvent,
}) => {
  return (
    <div className="hud-panel p-3 rounded-lg flex items-center space-x-3 overflow-x-auto select-none">
      <div className="flex items-center space-x-1.5 text-hud-cyan font-mono text-xs shrink-0 pr-3 border-r border-blue-900/40">
        <Clock className="w-4 h-4 text-hud-cyan" />
        <span className="font-bold">EVENT HORIZON</span>
      </div>

      <div className="flex items-center space-x-3 overflow-x-auto py-1">
        {events.slice(0, 10).map((evt) => {
          const isSelected = selectedEventId === evt.eventId;
          const isExtreme = evt.intensity.startsWith('X') || evt.geomagneticIndex >= 7;

          const badgeColor =
            isExtreme ? 'border-red-500/70 bg-red-950/40 text-red-300' :
            evt.intensity.startsWith('M') ? 'border-amber-500/70 bg-amber-950/40 text-amber-300' :
            'border-cyan-500/50 bg-cyan-950/30 text-cyan-300';

          return (
            <button
              key={evt.eventId}
              onClick={() => onSelectEvent(evt)}
              className={`px-3 py-1.5 rounded border font-mono text-left transition-all shrink-0 ${isSelected ? 'border-hud-cyan bg-blue-900/60 shadow-glow-cyan' : `${badgeColor} hover:border-hud-cyan/80`}`}
            >
              <div className="flex items-center space-x-1.5 text-[11px] font-bold">
                <Zap className="w-3 h-3 text-hud-amber" />
                <span>{evt.intensity} {evt.eventType.replace('_', ' ')}</span>
              </div>
              <div className="text-[9px] text-slate-400 mt-0.5">
                Kp: {evt.geomagneticIndex} | {Math.round(evt.solarWindSpeed)} km/s
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};
