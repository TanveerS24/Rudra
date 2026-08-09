import React, { useMemo, useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';

export const EarthGlobe: React.FC = () => {
  const earthRef = useRef<THREE.Mesh>(null);
  const atmosphereRef = useRef<THREE.Mesh>(null);

  // Generate high-resolution procedural Earth texture with continents and coordinate HUD grid
  const earthTexture = useMemo(() => {
    const canvas = document.createElement('canvas');
    canvas.width = 2048;
    canvas.height = 1024;
    const ctx = canvas.getContext('2d');
    if (!ctx) return new THREE.Texture();

    // Deep ocean blue gradient
    const oceanGrad = ctx.createLinearGradient(0, 0, 0, canvas.height);
    oceanGrad.addColorStop(0, '#041026');
    oceanGrad.addColorStop(0.5, '#020b1a');
    oceanGrad.addColorStop(1, '#041026');
    ctx.fillStyle = oceanGrad;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Latitude & Longitude HUD grid lines
    ctx.strokeStyle = 'rgba(59, 130, 246, 0.15)';
    ctx.lineWidth = 1;

    // Latitudes
    for (let lat = -80; lat <= 80; lat += 20) {
      const y = ((90 - lat) / 180) * canvas.height;
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(canvas.width, y);
      ctx.stroke();
    }
    // Longitudes
    for (let lon = -180; lon <= 180; lon += 30) {
      const x = ((lon + 180) / 360) * canvas.width;
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, canvas.height);
      ctx.stroke();
    }

    // Equator & Prime Meridian highlight
    ctx.strokeStyle = 'rgba(0, 240, 255, 0.35)';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(0, canvas.height / 2);
    ctx.lineTo(canvas.width, canvas.height / 2);
    ctx.stroke();

    // Draw stylized continents
    ctx.fillStyle = '#0f314d';
    ctx.strokeStyle = '#00f0ff';
    ctx.lineWidth = 1.5;

    const drawLandmass = (pts: [number, number][]) => {
      ctx.beginPath();
      pts.forEach(([lon, lat], i) => {
        const x = ((lon + 180) / 360) * canvas.width;
        const y = ((90 - lat) / 180) * canvas.height;
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      });
      ctx.closePath();
      ctx.fill();
      ctx.stroke();
    };

    // North America
    drawLandmass([[-165, 65], [-140, 70], [-100, 75], [-60, 60], [-70, 45], [-80, 25], [-100, 20], [-120, 35], [-130, 50], [-165, 60]]);
    // South America
    drawLandmass([[-80, 10], [-50, 0], [-35, -10], [-55, -45], [-70, -55], [-75, -20], [-80, 0]]);
    // Eurasia
    drawLandmass([[-10, 40], [30, 70], [100, 75], [170, 65], [140, 35], [105, 10], [75, 15], [45, 30], [10, 35], [-5, 45]]);
    // Africa
    drawLandmass([[-15, 35], [30, 32], [50, 10], [40, -10], [20, -35], [10, -10], [-15, 15]]);
    // Australia
    drawLandmass([[115, -15], [150, -15], [150, -38], [115, -35]]);
    // Antarctica
    drawLandmass([[-180, -75], [180, -75], [180, -90], [-180, -90]]);

    const texture = new THREE.CanvasTexture(canvas);
    texture.wrapS = THREE.RepeatWrapping;
    texture.wrapT = THREE.ClampToEdgeWrapping;
    return texture;
  }, []);

  useFrame((_, delta) => {
    if (earthRef.current) {
      earthRef.current.rotation.y += delta * 0.04;
    }
    if (atmosphereRef.current) {
      atmosphereRef.current.rotation.y += delta * 0.05;
    }
  });

  return (
    <group>
      {/* Core Earth Sphere */}
      <mesh ref={earthRef}>
        <sphereGeometry args={[4, 64, 64]} />
        <meshStandardMaterial
          map={earthTexture}
          roughness={0.65}
          metalness={0.2}
          emissive="#020917"
          emissiveIntensity={0.6}
        />
      </mesh>

      {/* Atmospheric Glow Shell */}
      <mesh ref={atmosphereRef} scale={1.04}>
        <sphereGeometry args={[4, 48, 48]} />
        <meshStandardMaterial
          color="#00f0ff"
          transparent
          opacity={0.12}
          side={THREE.BackSide}
          blending={THREE.AdditiveBlending}
        />
      </mesh>
    </group>
  );
};
