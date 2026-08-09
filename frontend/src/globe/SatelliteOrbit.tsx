import React, { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { Satellite } from '../types';

interface SatelliteOrbitProps {
  satellites: Satellite[];
  onSelectSatellite?: (satellite: Satellite) => void;
  selectedSatelliteId?: string | null;
}

export const SatelliteOrbit: React.FC<SatelliteOrbitProps> = ({
  satellites,
  onSelectSatellite,
  selectedSatelliteId,
}) => {
  return (
    <group>
      {/* Orbit Track Rings */}
      <mesh rotation={[Math.PI / 3, 0, 0]}>
        <ringGeometry args={[4.79, 4.81, 64]} />
        <meshBasicMaterial color="#3b82f6" transparent opacity={0.25} side={THREE.DoubleSide} />
      </mesh>

      <mesh rotation={[Math.PI / 4, Math.PI / 6, 0]}>
        <ringGeometry args={[6.19, 6.21, 64]} />
        <meshBasicMaterial color="#a855f7" transparent opacity={0.25} side={THREE.DoubleSide} />
      </mesh>

      <mesh rotation={[0, 0, 0]}>
        <ringGeometry args={[7.79, 7.81, 64]} />
        <meshBasicMaterial color="#00f0ff" transparent opacity={0.3} side={THREE.DoubleSide} />
      </mesh>

      {/* Satellite Node Markers */}
      {satellites.map((sat, index) => (
        <SatelliteNode
          key={sat.satelliteId}
          satellite={sat}
          index={index}
          total={satellites.length}
          isSelected={selectedSatelliteId === sat.satelliteId}
          onSelect={() => onSelectSatellite?.(sat)}
        />
      ))}
    </group>
  );
};

interface SatelliteNodeProps {
  satellite: Satellite;
  index: number;
  total: number;
  isSelected: boolean;
  onSelect: () => void;
}

const SatelliteNode: React.FC<SatelliteNodeProps> = ({
  satellite,
  index,
  total,
  isSelected,
  onSelect,
}) => {
  const meshRef = useRef<THREE.Group>(null);

  // Determine orbital radius based on orbit type
  const radius =
    satellite.orbitType === 'LEO' ? 4.8 :
    satellite.orbitType === 'MEO' ? 6.2 :
    satellite.orbitType === 'GEO' ? 7.8 : 8.5;

  const speed =
    satellite.orbitType === 'LEO' ? 0.35 :
    satellite.orbitType === 'MEO' ? 0.18 :
    satellite.orbitType === 'GEO' ? 0.08 : 0.12;

  const inclination = (satellite.inclinationDeg || 28.5) * (Math.PI / 180);
  const initialPhase = (index / total) * Math.PI * 2;

  // Health status color
  const statusColor =
    satellite.healthStatus === 'CRITICAL' ? '#ef4444' :
    satellite.healthStatus === 'DEGRADED' || satellite.operationalStatus === 'SAFE_MODE' ? '#f59e0b' :
    '#10b981';

  useFrame(({ clock }) => {
    const t = clock.getElapsedTime() * speed + initialPhase;
    if (meshRef.current) {
      const x = radius * Math.cos(t);
      const z = radius * Math.sin(t) * Math.cos(inclination);
      const y = radius * Math.sin(t) * Math.sin(inclination);

      meshRef.current.position.set(x, y, z);
      meshRef.current.rotation.y += 0.02;
    }
  });

  return (
    <group
      ref={meshRef}
      onClick={(e) => {
        e.stopPropagation();
        onSelect();
      }}
    >
      {/* Central Satellite Body */}
      <mesh>
        <boxGeometry args={[0.16, 0.16, 0.16]} />
        <meshStandardMaterial
          color={isSelected ? '#00f0ff' : '#94a3b8'}
          metalness={0.8}
          roughness={0.2}
          emissive={isSelected ? '#00f0ff' : '#000'}
          emissiveIntensity={isSelected ? 0.6 : 0}
        />
      </mesh>

      {/* Solar Array Panels */}
      <mesh position={[0.22, 0, 0]}>
        <boxGeometry args={[0.24, 0.08, 0.02]} />
        <meshStandardMaterial color="#1e3a8a" metalness={0.9} roughness={0.1} />
      </mesh>
      <mesh position={[-0.22, 0, 0]}>
        <boxGeometry args={[0.24, 0.08, 0.02]} />
        <meshStandardMaterial color="#1e3a8a" metalness={0.9} roughness={0.1} />
      </mesh>

      {/* Health Beacon Halo */}
      <mesh>
        <sphereGeometry args={[0.26, 16, 16]} />
        <meshBasicMaterial
          color={statusColor}
          transparent
          opacity={isSelected ? 0.5 : 0.25}
          wireframe
        />
      </mesh>
    </group>
  );
};
