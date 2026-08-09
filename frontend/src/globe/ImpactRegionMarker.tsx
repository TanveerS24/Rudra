import React, { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { LocationCoordinates } from '../types';

interface ImpactRegionMarkerProps {
  location: LocationCoordinates;
  intensity: string;
  geomagneticIndex: number;
}

export const ImpactRegionMarker: React.FC<ImpactRegionMarkerProps> = ({
  location,
  intensity,
  geomagneticIndex,
}) => {
  const pulseRef = useRef<THREE.Mesh>(null);
  const ringRef = useRef<THREE.Mesh>(null);

  // Convert latitude and longitude to 3D Cartesian coordinates on sphere R=4.02
  const R = 4.04;
  const phi = (90 - location.latitude) * (Math.PI / 180);
  const theta = (location.longitude + 180) * (Math.PI / 180);

  const x = -(R * Math.sin(phi) * Math.cos(theta));
  const z = R * Math.sin(phi) * Math.sin(theta);
  const y = R * Math.cos(phi);

  // Determine severity color
  const isCritical = intensity.toUpperCase().startsWith('X') || geomagneticIndex >= 8;
  const isHigh = intensity.toUpperCase().startsWith('M') || geomagneticIndex >= 6;
  const isModerate = geomagneticIndex >= 4;

  const color = isCritical ? '#ef4444' : isHigh ? '#f97316' : isModerate ? '#f59e0b' : '#00f0ff';

  useFrame(({ clock }) => {
    const t = clock.getElapsedTime();
    if (pulseRef.current) {
      const scale = 1 + Math.sin(t * (isCritical ? 6 : 3)) * 0.35;
      pulseRef.current.scale.set(scale, scale, scale);
    }
    if (ringRef.current) {
      const ringScale = 1 + ((t * 2) % 2.5);
      ringRef.current.scale.set(ringScale, ringScale, ringScale);
      const mat = ringRef.current.material as THREE.MeshBasicMaterial;
      if (mat) {
        mat.opacity = Math.max(0, 0.8 - (ringScale / 2.5));
      }
    }
  });

  return (
    <group position={[x, y, z]}>
      {/* Central High-Intensity Impact Core */}
      <mesh ref={pulseRef}>
        <sphereGeometry args={[0.12, 16, 16]} />
        <meshBasicMaterial color={color} />
      </mesh>

      {/* Pulsing Terrestrial Shockwave Ring */}
      <mesh ref={ringRef} rotation={[Math.PI / 2, 0, 0]}>
        <ringGeometry args={[0.16, 0.32, 32]} />
        <meshBasicMaterial
          color={color}
          transparent
          opacity={0.6}
          side={THREE.DoubleSide}
        />
      </mesh>

      {/* Elevation Beam Marker */}
      <mesh position={[0, 0.3, 0]}>
        <cylinderGeometry args={[0.015, 0.015, 0.6, 8]} />
        <meshBasicMaterial color={color} transparent opacity={0.7} />
      </mesh>
    </group>
  );
};
