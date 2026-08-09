# REST & WebSocket API Specification

All REST calls from the frontend pass through the **API Gateway** on port `8080` (or `http://localhost:8080/api/v1/...`).

---

## 1. REST Endpoints

### Space Weather Events
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/events` | List recent space weather events (query: `limit`). |
| `GET` | `/api/v1/events/latest` | Retrieve the latest processed space weather event. |
| `GET` | `/api/v1/events/{id}` | Retrieve a specific space weather event by ID. |
| `POST` | `/api/v1/events` | Ingest a space weather event into the pipeline. |

### Satellite Fleet
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/satellites` | List all satellites in the virtual constellation. |
| `GET` | `/api/v1/satellites/{id}` | Retrieve satellite details and orbit parameters. |
| `PUT` | `/api/v1/satellites/{id}/status` | Update satellite health or operational status. |

### Risk Assessments
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/risk` | List recent fleet risk assessments. |
| `GET` | `/api/v1/risk/{id}` | Retrieve specific risk assessment by ID. |
| `GET` | `/api/v1/risk/event/{eventId}` | Get risk assessments across all satellites for an event. |

### Decision Directives & Recommendations
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/recommendations` | List active decision recommendations. |
| `PUT` | `/api/v1/recommendations/{id}/status` | Update directive status (`EXECUTED`, `DISMISSED`). |

### Alerts
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/alerts` | List active system space weather alerts. |
| `PUT` | `/api/v1/alerts/{id}/acknowledge` | Acknowledge active alert. |

### Dashboard Telemetry Summary
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/dashboard/summary` | Consolidated telemetry snapshot for mission control HUD. |

### Simulator Controls
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/simulator/config` | Get active simulation configuration. |
| `PUT` | `/api/v1/simulator/config` | Update interval, worker count, mode, default intensity. |
| `POST` | `/api/v1/simulator/start` | Start scheduled scenario generation. |
| `POST` | `/api/v1/simulator/pause` | Pause scheduled scenario generation. |
| `POST` | `/api/v1/simulator/reset` | Reset simulation state. |
| `POST` | `/api/v1/simulator/generate` | Trigger immediate single scenario generation. |
| `GET` | `/api/v1/simulator/status` | Worker queue metrics and generation stats. |
| `GET` | `/api/v1/simulator/memories` | Inspect persistent semantic RAG memories. |
| `POST` | `/api/v1/simulator/feedback` | Submit accuracy & usefulness quality score. |

### Health Checks
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` | Consolidated system health status. |
| `GET` | `/ready` | Subsystem readiness verification. |

---

## 2. WebSocket Telemetry Events

WebSocket Server listens on port `8085` (`ws://localhost:8085`).

### Message Format:
```json
{
  "type": "SPACE_WEATHER_EVENT",
  "timestamp": "2026-08-09T12:00:00Z",
  "payload": { ... },
  "correlationId": "REQ-7AF39D12"
}
```

### Event Types:
- `SPACE_WEATHER_EVENT`: New space weather scenario generated and ingested.
- `RISK_UPDATED`: Array of risk assessments computed across the constellation.
- `RECOMMENDATION_CREATED`: Actionable decision directives synthesized.
- `ALERT_CREATED`: Space weather severity alert triggered.
- `SATELLITE_STATUS_CHANGED`: Satellite health or operational mode transition.
