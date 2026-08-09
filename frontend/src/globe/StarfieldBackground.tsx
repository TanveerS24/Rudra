import React, { useMemo, useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';

export const StarfieldBackground: React.FC = () => {
  const pointsRef = useRef<THREE.Points>(null);

  const [positions, colors] = useMemo(() => {
    const count = 3000;
    const pos = new Float32Array(count * 3);
    const col = new Float32Array(count * 3);

    for (let i = 0; i < count; i++) {
      const radius = 80 + Math.random() * 120;
      const theta = 2 * Math.PI * Math.random();
      const phi = Math.acos(2 * Math.random() - 1);

      pos[i * 3] = radius * Math.sin(phi) * Math.cos(theta);
      pos[i * 3 + 1] = radius * Math.sin(phi) * Math.sin(theta);
      pos[i * 3 + 2] = radius * Math.cos(phi);

      // Star colors: cold blue/cyan, soft white, amber tints
      const r = Math.random();
      if (r > 0.8) {
        col[i * 3] = 0.4; col[i * 3 + 1] = 0.8; col[i * 3 + 2] = 1.0; // Cyan star
      } else if (r > 0.6) {
        col[i * 3] = 1.0; col[i * 3 + 1] = 0.8; col[i * 3 + 2] = 0.4; // Amber star
      } else {
        col[i * 3] = 0.9; col[i * 3 + 1] = 0.95; col[i * 3 + 2] = 1.0; // White star
      }
    }
    return [pos, col];
  }, []);

  useFrame((_, delta) => {
    if (pointsRef.current) {
      pointsRef.current.rotation.y += delta * 0.015;
      pointsRef.current.rotation.x += delta * 0.005;
    }
  });

  return (
    <points ref={pointsRef}>
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          args={[positions, 3]}
        />
        <bufferAttribute
          attach="attributes-color"
          args={[colors, 3]}
        />
      </bufferGeometry>
      <pointsMaterial
        size={1.2}
        vertexColors
        transparent
        opacity={0.85}
        sizeAttenuation
      />
    </points>
  );
};
