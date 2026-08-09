-- ============================================================================
-- INTEGRATED SPACE WEATHER MONITORING & SATELLITE OPERATIONS DECISION SUPPORT
-- Database Migration: V1__initial_schema.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS spaceweather_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE spaceweather_db;

-- 1. Space Weather Events
CREATE TABLE IF NOT EXISTS space_weather_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    event_type VARCHAR(64) NOT NULL,
    intensity VARCHAR(32) NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 30,
    solar_wind_speed DOUBLE NOT NULL DEFAULT 400.0,
    geomagnetic_index INT NOT NULL DEFAULT 3,
    radiation_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    origin_latitude DOUBLE NOT NULL DEFAULT 0.0,
    origin_longitude DOUBLE NOT NULL DEFAULT 0.0,
    impact_latitude DOUBLE NOT NULL DEFAULT 0.0,
    impact_longitude DOUBLE NOT NULL DEFAULT 0.0,
    affected_regions JSON,
    impact_description TEXT,
    confidence DOUBLE NOT NULL DEFAULT 0.85,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_events_event_id (event_id),
    INDEX idx_events_event_type (event_type),
    INDEX idx_events_created_at (created_at),
    INDEX idx_events_intensity (intensity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Satellites Fleet
CREATE TABLE IF NOT EXISTS satellites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    satellite_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    mission_type VARCHAR(64) NOT NULL,
    orbit_type VARCHAR(32) NOT NULL,
    altitude_km DOUBLE NOT NULL,
    inclination_deg DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL DEFAULT 0.0,
    longitude DOUBLE NOT NULL DEFAULT 0.0,
    health_status VARCHAR(32) NOT NULL DEFAULT 'NOMINAL',
    radiation_sensitivity VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    communication_sensitivity VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    navigation_sensitivity VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    operational_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_satellites_satellite_id (satellite_id),
    INDEX idx_satellites_orbit (orbit_type),
    INDEX idx_satellites_health (health_status),
    INDEX idx_satellites_operational (operational_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Risk Assessments
CREATE TABLE IF NOT EXISTS risk_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id VARCHAR(64) NOT NULL UNIQUE,
    event_id VARCHAR(64) NOT NULL,
    satellite_id VARCHAR(64) NOT NULL,
    deterministic_score DOUBLE NOT NULL,
    final_score DOUBLE NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    primary_factors JSON,
    potential_effects JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_risk_assessment_id (assessment_id),
    INDEX idx_risk_event_id (event_id),
    INDEX idx_risk_satellite_id (satellite_id),
    INDEX idx_risk_level (risk_level),
    INDEX idx_risk_created_at (created_at),
    CONSTRAINT fk_risk_event FOREIGN KEY (event_id) REFERENCES space_weather_events(event_id) ON DELETE CASCADE,
    CONSTRAINT fk_risk_satellite FOREIGN KEY (satellite_id) REFERENCES satellites(satellite_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Operational Recommendations
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recommendation_id VARCHAR(64) NOT NULL UNIQUE,
    event_id VARCHAR(64) NOT NULL,
    assessment_id VARCHAR(64),
    satellite_id VARCHAR(64),
    action VARCHAR(255) NOT NULL,
    reasoning TEXT NOT NULL,
    expected_impact TEXT NOT NULL,
    confidence DOUBLE NOT NULL DEFAULT 0.85,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rec_recommendation_id (recommendation_id),
    INDEX idx_rec_event_id (event_id),
    INDEX idx_rec_assessment_id (assessment_id),
    INDEX idx_rec_status (status),
    CONSTRAINT fk_rec_event FOREIGN KEY (event_id) REFERENCES space_weather_events(event_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. System Alerts
CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id VARCHAR(64) NOT NULL UNIQUE,
    event_id VARCHAR(64),
    severity VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alerts_alert_id (alert_id),
    INDEX idx_alerts_severity (severity),
    INDEX idx_alerts_created_at (created_at),
    INDEX idx_alerts_acknowledged (acknowledged)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Space Missions
CREATE TABLE IF NOT EXISTS missions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    target_satellite_id VARCHAR(64),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_missions_mission_id (mission_id),
    INDEX idx_missions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Simulation Configurations
CREATE TABLE IF NOT EXISTS simulation_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interval_seconds INT NOT NULL DEFAULT 15,
    worker_count INT NOT NULL DEFAULT 2,
    mode VARCHAR(32) NOT NULL DEFAULT 'HYBRID_LLM',
    default_intensity VARCHAR(32) NOT NULL DEFAULT 'MODERATE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Persistent Semantic RAG Memory
CREATE TABLE IF NOT EXISTS rag_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chunk_id VARCHAR(64) NOT NULL UNIQUE,
    source_event_id VARCHAR(64),
    chunk_type VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    importance DOUBLE NOT NULL DEFAULT 1.0,
    feedback_score DOUBLE NOT NULL DEFAULT 0.5,
    embedding_json JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_memory_chunk_id (chunk_id),
    INDEX idx_memory_source_event (source_event_id),
    INDEX idx_memory_chunk_type (chunk_type),
    INDEX idx_memory_feedback (feedback_score),
    INDEX idx_memory_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Feedback Scoring
CREATE TABLE IF NOT EXISTS feedback_scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    score_id VARCHAR(64) NOT NULL UNIQUE,
    target_id VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    accuracy_score DOUBLE NOT NULL,
    usefulness_score DOUBLE NOT NULL,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_feedback_target (target_id, target_type),
    INDEX idx_feedback_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
