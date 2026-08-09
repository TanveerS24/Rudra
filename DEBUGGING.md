# Observability, Tracing & Debugging

## 1. Structured Correlation Tracing

Every request across all microservices carries a correlation ID:

```text
[SERVICE-NAME] [REQ-XXXXX] [EVENT-ID] Message details
```

### Example Log Trace:
```text
2026-08-09T12:00:00 [API-GATEWAY] [REQ-98F12A] Proxying POST /api/v1/events -> http://localhost:8081/api/v1/events
2026-08-09T12:00:00 [MAIN-BACKEND] [REQ-98F12A] [EVT-SIM-48A] Processing space weather event: SOLAR_FLARE (Intensity: X2.4, Kp: 7)
2026-08-09T12:00:00 [MAIN-BACKEND] [REQ-98F12A] [EVT-SIM-48A] Risk calculation completed. Assessed 8 satellites across constellation.
2026-08-09T12:00:00 [API-GATEWAY] [REQ-98F12A] Completed proxy POST /api/v1/events status=201 duration=14ms
```

---

## 2. Health & Readiness Diagnostics

Verify health and upstream status:
```bash
curl http://localhost:8080/health
```

Sample Response:
```json
{
  "service": "api-gateway",
  "status": "UP",
  "timestamp": "2026-08-09T12:00:00Z",
  "details": {
    "api-gateway": "UP",
    "main-backend": "UP",
    "simulation-service": "UP"
  }
}
```
