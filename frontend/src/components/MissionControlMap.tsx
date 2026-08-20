import React, { useState, useEffect, useMemo, useRef } from 'react';
import {
  Map,
  MapControls,
  MapMarker,
  MarkerContent,
  MarkerLabel,
  MarkerPopup,
  MapRoute,
} from '@/components/ui/map';
import { Card } from '@/components/ui/card';
import { LocationCoordinates, Satellite } from '../types';
import { Radio, Satellite as SatelliteIcon, ShieldAlert, Globe2, Map as MapIcon, Crosshair } from 'lucide-react';
import type { MapRef } from '@/components/ui/map';

interface MissionControlMapProps {
  impactLocation?: LocationCoordinates;
  intensity?: string;
  geomagneticIndex?: number;
  satellites: Satellite[];
  selectedSatelliteId?: string | null;
  onSelectSatellite?: (satellite: Satellite) => void;
}

export const MissionControlMap: React.FC<MissionControlMapProps> = ({
  impactLocation = { latitude: 28.5, longitude: -80.6 },
  intensity = 'M2.0',
  geomagneticIndex = 5,
  satellites,
  selectedSatelliteId,
  onSelectSatellite,
}) => {
  const [isGlobeProjection, setIsGlobeProjection] = useState<boolean>(true);
  const [activePopupSatId, setActivePopupSatId] = useState<string | null>(null);
  const [orbitTime, setOrbitTime] = useState<number>(0);
  const mapRef = useRef<MapRef>(null);

  // Smooth orbital simulation timer
  useEffect(() => {
    let animationFrameId: number;
    let lastTime = performance.now();

    const animate = (time: number) => {
      const delta = (time - lastTime) / 1000;
      lastTime = time;
      setOrbitTime((prev) => prev + delta);
      animationFrameId = requestAnimationFrame(animate);
    };

    animationFrameId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(animationFrameId);
  }, []);

  // Auto-pan camera whenever a new space weather event or impact coordinate is detected
  useEffect(() => {
    if (impactLocation && mapRef.current) {
      try {
        mapRef.current.flyTo({
          center: [impactLocation.longitude, impactLocation.latitude],
          zoom: isGlobeProjection ? 1.25 : 2.2,
          duration: 1800,
          essential: true,
        });
      } catch (err) {
        // Map instance might be initializing
      }
    }
  }, [impactLocation?.latitude, impactLocation?.longitude, isGlobeProjection]);

  const handleToggleProjection = () => {
    const nextIsGlobe = !isGlobeProjection;
    setIsGlobeProjection(nextIsGlobe);
    if (mapRef.current && impactLocation) {
      try {
        mapRef.current.easeTo({
          center: [impactLocation.longitude, impactLocation.latitude],
          zoom: nextIsGlobe ? 1.25 : 2.2,
          duration: 1200,
        });
      } catch (_) {}
    }
  };

  // Compute animated orbital coordinates and trailing light paths for each satellite
  const animatedSatellites = useMemo(() => {
    return satellites.map((sat, idx) => {
      // Speed factor based on orbital regime (LEO fast, MEO moderate, GEO slow)
      const speed = sat.orbitType === 'LEO' ? 2.8 : sat.orbitType === 'MEO' ? 1.5 : 0.6;
      const baseLng = sat.longitude;
      const inclination = sat.inclinationDeg || (sat.orbitType === 'GEO' ? 0.5 : 51.6);
      const phase = idx * 45;

      // Current live orbital coordinate
      const rawLng = (baseLng + orbitTime * speed * 2) % 360;
      const liveLng = rawLng > 180 ? rawLng - 360 : rawLng < -180 ? rawLng + 360 : rawLng;
      const liveLat = Math.sin(((liveLng + phase) * Math.PI) / 180) * inclination;

      return {
        ...sat,
        liveLng,
        liveLat,
      };
    });
  }, [satellites, orbitTime]);

  const selectedSat = animatedSatellites.find((s) => s.satelliteId === selectedSatelliteId);

  return (
    <Card className="h-[400px] w-full p-0 overflow-hidden relative border-blue-900/50 bg-space-950/90 shadow-2xl flex flex-col">
      {/* Top Telemetry Header Bar */}
      <div className="absolute top-2.5 left-2.5 z-20 flex items-center space-x-2 pointer-events-none">
        <div className="bg-space-900/90 border border-hud-cyan/50 backdrop-blur-md px-2.5 py-1 rounded flex items-center space-x-1.5 text-[11px] font-mono text-hud-cyan shadow-glow-cyan">
          <span className="inline-block w-2 h-2 rounded-full bg-hud-cyan animate-ping" />
          <span className="font-semibold tracking-wider">
            {isGlobeProjection ? 'GEOSPATIAL RADAR (3D GLOBE)' : 'TERRESTRIAL IMPACT RADAR (2D)'}
          </span>
        </div>
        <div className="bg-space-900/80 border border-blue-900/60 backdrop-blur-md px-2 py-0.5 rounded text-[10px] font-mono text-slate-300">
          IMPACT: <span className="text-yellow-400 font-bold">{impactLocation.latitude.toFixed(1)}°N, {impactLocation.longitude.toFixed(1)}°E</span> | KP: <span className="text-orange-400 font-bold">{geomagneticIndex}</span>
        </div>
      </div>

      {/* Projection Switcher & Controls */}
      <div className="absolute top-2.5 right-2.5 z-20 flex items-center space-x-2">
        <button
          onClick={handleToggleProjection}
          className="bg-space-900/90 hover:bg-space-800 border border-hud-cyan/40 hover:border-hud-cyan text-hud-cyan text-[11px] font-mono px-2.5 py-1 rounded flex items-center space-x-1.5 backdrop-blur-md transition-all shadow-sm cursor-pointer"
          title="Toggle 3D Globe / 2D Mercator Projection"
        >
          {isGlobeProjection ? (
            <>
              <Globe2 className="w-3 h-3" />
              <span>3D GLOBE</span>
            </>
          ) : (
            <>
              <MapIcon className="w-3 h-3" />
              <span>2D MAP</span>
            </>
          )}
        </button>
      </div>

      {/* MapLibre Map Component */}
      <div className="flex-1 w-full h-full relative">
        <Map
          ref={mapRef}
          center={[impactLocation.longitude, impactLocation.latitude]}
          zoom={isGlobeProjection ? 1.25 : 2.2}
          projection={isGlobeProjection ? { type: 'globe' } : undefined}
          theme="dark"
          className="w-full h-full"
        >
          <MapControls position="bottom-right" showZoom showCompass showFullscreen />

          {/* Terrestrial Impact Shockwave Marker */}
          {impactLocation && (
            <MapMarker
              longitude={impactLocation.longitude}
              latitude={impactLocation.latitude}
            >
              <MarkerContent>
                <div className="relative flex items-center justify-center cursor-pointer group">
                  <div className="absolute w-12 h-12 rounded-full bg-red-500/20 animate-ping" />
                  <div className="absolute w-8 h-8 rounded-full bg-orange-500/40 animate-pulse" />
                  <div className="w-5 h-5 rounded-full bg-red-600 border-2 border-yellow-300 shadow-[0_0_15px_#ff0055] flex items-center justify-center">
                    <ShieldAlert className="w-3 h-3 text-white" />
                  </div>
                </div>
              </MarkerContent>
              <MarkerLabel position="top">
                <span className="bg-red-950/90 text-red-300 border border-red-500/50 px-1.5 py-0.5 rounded text-[10px] font-mono tracking-wider font-bold">
                  EPICENTER [{intensity}]
                </span>
              </MarkerLabel>
              <MarkerPopup closeButton>
                <div className="p-1 space-y-1 font-mono text-xs">
                  <div className="font-bold text-red-400 flex items-center space-x-1">
                    <ShieldAlert className="w-4 h-4 text-red-500" />
                    <span>SOLAR FLUX IMPACT ZONE</span>
                  </div>
                  <div className="text-[11px] text-slate-300">
                    <div>Intensity Class: <span className="text-yellow-400 font-bold">{intensity}</span></div>
                    <div>Geomagnetic Index: <span className="text-orange-400 font-bold">Kp-{geomagneticIndex}</span></div>
                    <div>Coordinates: <span className="text-hud-cyan">{impactLocation.latitude.toFixed(2)}°, {impactLocation.longitude.toFixed(2)}°</span></div>
                  </div>
                </div>
              </MarkerPopup>
            </MapMarker>
          )}

          {/* Active Constellation Satellites (Rendered Only in 3D Orbital Globe Mode) */}
          {isGlobeProjection &&
            animatedSatellites.map((sat) => {
              const isSelected = sat.satelliteId === selectedSatelliteId;
              const isCritical = sat.healthStatus === 'CRITICAL';
              const isDegraded = sat.healthStatus === 'DEGRADED';

              const markerColor = isCritical
                ? 'bg-red-500 border-red-300 shadow-[0_0_10px_#ef4444]'
                : isDegraded
                  ? 'bg-amber-500 border-yellow-200 shadow-[0_0_10px_#f59e0b]'
                  : 'bg-hud-cyan border-white shadow-[0_0_10px_#00f0ff]';

              return (
                <MapMarker
                  key={sat.satelliteId}
                  longitude={sat.liveLng}
                  latitude={sat.liveLat}
                  onClick={() => {
                    onSelectSatellite?.(sat);
                    setActivePopupSatId(sat.satelliteId);
                  }}
                >
                  <MarkerContent>
                    <div
                      className={`relative p-1 rounded-full cursor-pointer transition-transform hover:scale-125 ${
                        isSelected ? 'ring-2 ring-hud-cyan ring-offset-2 ring-offset-space-950 scale-125' : ''
                      }`}
                    >
                      <div className={`w-3.5 h-3.5 rounded-full border ${markerColor} flex items-center justify-center`}>
                        <SatelliteIcon className="w-2 h-2 text-space-950" />
                      </div>
                    </div>
                  </MarkerContent>
                  <MarkerLabel position="bottom">
                    <span
                      className={`px-1 py-0.2 rounded text-[9px] font-mono ${
                        isSelected ? 'bg-hud-cyan text-space-950 font-bold' : 'bg-space-950/80 text-slate-300'
                      }`}
                    >
                      {sat.name}
                    </span>
                  </MarkerLabel>
                  {activePopupSatId === sat.satelliteId && (
                    <MarkerPopup
                      closeButton
                      onClose={() => setActivePopupSatId(null)}
                    >
                      <div className="p-1 space-y-1.5 font-mono text-xs">
                        <div className="font-bold text-hud-cyan flex items-center justify-between border-b border-blue-900/60 pb-1">
                          <span>{sat.name}</span>
                          <span className="text-[10px] px-1 rounded bg-blue-950 text-blue-300 border border-blue-700">
                            {sat.orbitType}
                          </span>
                        </div>
                        <div className="text-[11px] space-y-0.5 text-slate-300">
                          <div>
                            Health:{' '}
                            <span
                              className={
                                isCritical
                                  ? 'text-red-400 font-bold'
                                  : isDegraded
                                  ? 'text-amber-400 font-bold'
                                  : 'text-emerald-400 font-bold'
                              }
                            >
                              {sat.healthStatus}
                            </span>
                          </div>
                          <div>
                            Altitude: <span className="text-hud-cyan font-bold">{sat.altitudeKm} km</span>
                          </div>
                          <div>
                            Position:{' '}
                            <span className="text-slate-400">
                              {sat.latitude.toFixed(1)}°N, {sat.longitude.toFixed(1)}°E
                            </span>
                          </div>
                          <div>
                            Mission: <span className="text-slate-300">{sat.missionType}</span>
                          </div>
                        </div>
                      </div>
                    </MarkerPopup>
                  )}
                </MapMarker>
              );
            })}
        </Map>
      </div>

      {/* Bottom Mission Telemetry Overlay Banner */}
      <div className="absolute bottom-2 left-2 z-20 pointer-events-none flex items-center space-x-1.5 text-[10px] font-mono text-slate-400 bg-space-950/90 px-2.5 py-0.5 rounded border border-blue-900/60 backdrop-blur-sm">
        <Crosshair className="w-3 h-3 text-hud-cyan animate-spin shrink-0" />
        {isGlobeProjection ? (
          <>
            <span>TRACKING {satellites.length} NODES</span>
            {selectedSat && (
              <span className="text-hud-cyan font-bold truncate max-w-[120px]">| {selectedSat.name}</span>
            )}
          </>
        ) : (
          <span>TERRESTRIAL SURFACE FOCUS</span>
        )}
      </div>
    </Card>
  );
};
