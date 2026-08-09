# Database Architecture & Migrations

The database is built on **MySQL 8.0** with InnoDB storage engine and utf8mb4 encoding.

---

## 1. Schema Tables

```text
┌───────────────────────────┐       ┌───────────────────────────┐
│   space_weather_events    │       │        satellites         │
│ - event_id (PK, UNIQUE)   │       │ - satellite_id (PK, UNIQ) │
│ - event_type, intensity   │       │ - orbit_type, altitude_km │
│ - solar_wind, kp, rad     │       │ - sensitivities, health   │
└─────────────┬─────────────┘       └─────────────┬─────────────┘
              │ 1                                 │ 1
              │                                   │
              │ N                                 │ N
              ▼                                   ▼
┌───────────────────────────────────────────────────────────────┐
│                       risk_assessments                        │
│ - assessment_id (PK)                                          │
│ - event_id (FK -> space_weather_events)                       │
│ - satellite_id (FK -> satellites)                             │
│ - deterministic_score, final_score, risk_level                │
└───────────────────────────────┬───────────────────────────────┘
                                │ 1
                                │
                                │ N
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                        recommendations                        │
│ - recommendation_id (PK)                                      │
│ - event_id (FK), assessment_id, satellite_id                  │
│ - action, reasoning, expected_impact, confidence, status      │
└───────────────────────────────────────────────────────────────┘
```

Supporting tables:
- `alerts` (alert_id, event_id, severity, title, message, acknowledged, created_at)
- `missions` (mission_id, name, priority, status, target_satellite_id, start_time, created_at)
- `simulation_configurations` (interval_seconds, worker_count, mode, default_intensity, is_active)
- `rag_memory` (chunk_id, source_event_id, chunk_type, content, importance, feedback_score, embedding_json)
- `feedback_scores` (score_id, target_id, target_type, accuracy_score, usefulness_score, comments)

---

## 2. Versioned Migration Scripts

1. `database/migrations/V1__initial_schema.sql`: Table definitions, indexes, foreign key constraints, timestamps.
2. `database/seed/V2__seed_data.sql`: Virtual satellite fleet seeds (NOAA GOES-18, ISS Alpha, Sentinel-6, Starlink Leader, GPS-III, Hubble, Galileo, Chandra) and realistic historical space weather events.
