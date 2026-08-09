import React, { Suspense } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import { EarthGlobe } from './EarthGlobe';
import { StarfieldBackground } from './StarfieldBackground';
import { ImpactRegionMarker } from './ImpactRegionMarker';
import { SatelliteOrbit } from './SatelliteOrbit';
import { LocationCoordinates, Satellite } from '../types';

interface GlobeSceneProps {
  impactLocation?: LocationCoordinates;
  intensity?: string;
  geomagneticIndex?: number;
  satellites: Satellite[];
  onSelectSatellite?: (satellite: Satellite) => void;
  selectedSatelliteId?: string | null;
}

export const GlobeScene: React.FC<GlobeSceneProps> = ({
  impactLocation = { latitude: 28.5, longitude: -80.6 },
  intensity = 'M2.0',
  geomagneticIndex = 5,
  satellites,
  onSelectSatellite,
  selectedSatelliteId,
}) => {
  return (
    <div className="w-full h-full relative cursor-grab active:cursor-grabbing">
      <Canvas
        camera={{ position: [0, 5, 14], fov: 45 }}
        gl={{ antialias: true, alpha: true }}
      >
        <Suspense fallback={null}>
          <ambientLight intensity={0.7} />
          <directionalLight position={[15, 10, 10]} intensity={1.8} color="#fff" />
          <pointLight position={[-15, -10, -10]} intensity={0.4} color="#00f0ff" />

          {/* Dynamic Starfield Background */}
          <StarfieldBackground />

          {/* Interactive Earth Globe */}
          <EarthGlobe />

          {/* Dynamic Impact Region Plasma Shockwave Marker */}
          {impactLocation && (
            <ImpactRegionMarker
              location={impactLocation}
              intensity={intensity}
              geomagneticIndex={geomagneticIndex}
            />
          )}

          {/* Virtual Satellite Constellation Orbits & Nodes */}
          <SatelliteOrbit
            satellites={satellites}
            onSelectSatellite={onSelectSatellite}
            selectedSatelliteId={selectedSatelliteId}
          />

          <OrbitControls
            enablePan={false}
            minDistance={6}
            maxDistance={22}
            rotateSpeed={0.6}
            zoomSpeed={0.8}
            autoRotate={false}
          />
        </Suspense>
      </Canvas>

      {/* Futuristic 3D HUD Overlay Grid */}
      <div className="absolute top-3 left-3 pointer-events-none flex items-center space-x-2 text-[10px] text-hud-cyan/70 font-mono">
        <span className="inline-block w-2 h-2 rounded-full bg-hud-cyan animate-pulse" />
        <span>3D TERRESTRIAL & ORBITAL TELEMETRY RENDERER [ACTIVE]</span>
      </div>

      <div className="absolute bottom-3 right-3 pointer-events-none text-[10px] text-slate-400 font-mono bg-space-900/80 px-2 py-1 rounded border border-blue-900/40">
        ROTATE: DRAG | ZOOM: SCROLL | INSPECT: CLICK SAT
      </div>
    </div>
  );
};
