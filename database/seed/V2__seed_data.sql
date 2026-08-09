-- ============================================================================
-- INTEGRATED SPACE WEATHER MONITORING & SATELLITE OPERATIONS DECISION SUPPORT
-- Database Seed: V2__seed_data.sql
-- ============================================================================

USE spaceweather_db;

-- Seed Virtual Satellite Fleet
INSERT INTO satellites (satellite_id, name, mission_type, orbit_type, altitude_km, inclination_deg, latitude, longitude, health_status, radiation_sensitivity, communication_sensitivity, navigation_sensitivity, operational_status)
VALUES
  ('SAT-001-GOES18', 'NOAA GOES-18 Weather Monitor', 'Earth Observation', 'GEO', 35786.0, 0.05, 0.0, -137.2, 'NOMINAL', 'MEDIUM', 'HIGH', 'LOW', 'ACTIVE'),
  ('SAT-002-ISS', 'ISS Alpha Station', 'Human Spaceflight', 'LEO', 418.0, 51.64, 25.4, 45.1, 'NOMINAL', 'HIGH', 'HIGH', 'HIGH', 'ACTIVE'),
  ('SAT-003-SENT6', 'Sentinel-6 Michael Freilich', 'Ocean Topography', 'LEO', 1336.0, 66.04, -32.1, 115.3, 'NOMINAL', 'HIGH', 'MEDIUM', 'CRITICAL', 'ACTIVE'),
  ('SAT-004-STAR412', 'Starlink Group 4-12 Constellation Leader', 'Broadband Relay', 'LEO', 540.0, 53.22, 48.2, -122.4, 'NOMINAL', 'MEDIUM', 'CRITICAL', 'MEDIUM', 'ACTIVE'),
  ('SAT-005-GPS06', 'NAVSTAR GPS-III SV06', 'Global Positioning', 'MEO', 20180.0, 55.0, 12.8, -45.6, 'NOMINAL', 'HIGH', 'HIGH', 'CRITICAL', 'ACTIVE'),
  ('SAT-006-HST', 'Hubble Space Telescope', 'Deep Astronomy', 'LEO', 535.0, 28.47, -18.4, -65.2, 'NOMINAL', 'CRITICAL', 'MEDIUM', 'LOW', 'ACTIVE'),
  ('SAT-007-GAL24', 'Galileo FOC FM24 Elena', 'Navigation & Timing', 'MEO', 23222.0, 56.0, -4.2, 78.9, 'NOMINAL', 'HIGH', 'HIGH', 'CRITICAL', 'ACTIVE'),
  ('SAT-008-CHANDRA', 'Chandra X-Ray Observatory', 'Astrophysics', 'HEO', 64000.0, 28.5, 34.5, 142.1, 'NOMINAL', 'CRITICAL', 'HIGH', 'MEDIUM', 'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Seed Initial Simulation Configuration
INSERT INTO simulation_configurations (id, interval_seconds, worker_count, mode, default_intensity, is_active)
VALUES (1, 15, 2, 'HYBRID_LLM', 'MODERATE', TRUE)
ON DUPLICATE KEY UPDATE interval_seconds=VALUES(interval_seconds);

-- Seed Realistic Historical Space Weather Events
INSERT INTO space_weather_events (event_id, event_type, intensity, duration_minutes, solar_wind_speed, geomagnetic_index, radiation_level, origin_latitude, origin_longitude, impact_latitude, impact_longitude, affected_regions, impact_description, confidence)
VALUES
  ('EVT-HIST-2026-001', 'SOLAR_FLARE', 'X2.4', 45, 620.0, 7, 'HIGH', 14.2, 48.6, 28.5, -80.6,
   '["North America Subauroral", "North Atlantic Air Routes", "Arctic Polar Cap"]',
   'Major X2.4-class solar flare erupted from Active Region AR3664 producing strong high-frequency radio blackouts across sunlit hemisphere.',
   0.94),
  ('EVT-HIST-2026-002', 'CORONAL_MASS_EJECTION', 'M8.7', 90, 780.0, 8, 'CRITICAL', -18.5, -22.1, 55.4, 12.3,
   '["Northern Europe", "Scandinavian Power Grids", "High-Latitude LEO Corridors"]',
   'Fast Earth-directed CME with strong southward Bz magnetic component inducing G4-class geomagnetic storm and significant upper-atmospheric drag.',
   0.89),
  ('EVT-HIST-2026-003', 'GEOMAGNETIC_STORM', 'G3_STRONG', 120, 540.0, 6, 'ELEVATED', 0.0, 0.0, -42.8, 147.2,
   '["Southern Ocean", "Australasia Navigation Sectors", "Antarctic Research Bases"]',
   'Planetary K-index reached 6.67 triggering auroral electrojet currents and scintillation across L-band satellite navigation signals.',
   0.91)
ON DUPLICATE KEY UPDATE event_type=VALUES(event_type);

-- Seed Historical Risk Assessments
INSERT INTO risk_assessments (assessment_id, event_id, satellite_id, deterministic_score, final_score, risk_level, primary_factors, potential_effects)
VALUES
  ('RISK-HIST-001', 'EVT-HIST-2026-001', 'SAT-002-ISS', 78.5, 82.0, 'CRITICAL',
   '["High solar radiation flux (S3)", "LEO atmospheric expansion & drag", "Astronaut extravehicular activity exposure risk"]',
   '["Degraded telemetry uplink", "Enhanced drag requiring re-boost orbit correction", "EVA suspension protocol active"]'),
  ('RISK-HIST-002', 'EVT-HIST-2026-001', 'SAT-005-GPS06', 64.0, 68.0, 'HIGH',
   '["MEO radiation belt particle injection", "Ionospheric delay error increase"]',
   '["L-band pseudo-range timing drift", "Ephemeris broadcast degradation"]'),
  ('RISK-HIST-003', 'EVT-HIST-2026-002', 'SAT-004-STAR412', 72.0, 75.0, 'CRITICAL',
   '["Thermospheric heating expanding 500km boundary", "Severe drag on planar array"]',
   '["Orbital decay rate increased by 240%", "Attitude trim required to minimize surface cross-section"]')
ON DUPLICATE KEY UPDATE final_score=VALUES(final_score);

-- Seed Historical Recommendations
INSERT INTO recommendations (recommendation_id, event_id, assessment_id, satellite_id, action, reasoning, expected_impact, confidence, status)
VALUES
  ('REC-HIST-001', 'EVT-HIST-2026-001', 'RISK-HIST-001', 'SAT-002-ISS',
   'Suspend Scheduled EVAs & Activate Radiation Shelter Protocol',
   'Proton flux at >10 MeV exceeds safety threshold of 100 pfu in LEO polar crossing sectors.',
   'Protects crew health and minimizes radiation dose during orbital passes 4 through 7.',
   0.96, 'EXECUTED'),
  ('REC-HIST-002', 'EVT-HIST-2026-002', 'RISK-HIST-003', 'SAT-004-STAR412',
   'Orient Solar Arrays into Knife-Edge Low-Drag Mode',
   'Atmospheric density at 540km increased by 3.2x due to CME energy deposition in upper thermosphere.',
   'Reduces orbital decay velocity and prevents premature de-orbiting of constellation sector 4-12.',
   0.92, 'EXECUTED')
ON DUPLICATE KEY UPDATE action=VALUES(action);

-- Seed Alerts
INSERT INTO alerts (alert_id, event_id, severity, title, message, acknowledged)
VALUES
  ('ALT-001', 'EVT-HIST-2026-001', 'CRITICAL', 'X2.4 Solar Flare Detected', 'Severe radiation flux impacting sunlit hemisphere and polar satellite constellations.', TRUE),
  ('ALT-002', 'EVT-HIST-2026-002', 'WARNING', 'G4 Geomagnetic Storm Watch', 'Earth-directed CME arrival imminent within 12-18 hours. Satellite operators advised to review safe-mode protocols.', FALSE)
ON DUPLICATE KEY UPDATE title=VALUES(title);
