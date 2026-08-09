# System Architecture & Design Specification

## Overview

The **Integrated Space Weather Monitoring and Satellite Operations Decision Support System** is constructed following strict Domain-Driven Design (DDD) and distributed microservice patterns in **Pure Java 21** without Spring or Hibernate.

---

## 1. Architectural Boundaries

```text
┌─────────────────────────────────────────────────────────────┐
│                      React 3D Frontend                      │
│     (Three.js, R3F, Recharts, Lucide, Tailwind, WebSockets) │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP & WS
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   Service 1: API Gateway                    │
│      - Reverse Proxy & Route Resolution                     │
│      - Correlation ID Injection ([REQ-XXXXX])               │
│      - Health Status Aggregator                             │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
       /api/v1/simulator/*             /api/v1/events/*, risk, etc.
               ▼                               ▼
┌─────────────────────────────┐ ┌─────────────────────────────┐
│ Service 2: Simulation Svc   │ │ Service 3: Main Backend     │
│ - Bounded Queue & Workers   │ │ - Idempotency & Validation  │
│ - Semantic Chunker          │ │ - Hybrid Risk Engine        │
│ - Vector Memory (Cosine Sim)│ │ - Decision Directives       │
│ - Ollama Llama 3.1:8B Client│ │ - MySQL JDBC (HikariCP)     │
│ - Feedback Scoring Weighting│ │ - WebSocket Broadcaster     │
└─────────────────────────────┘ └─────────────────────────────┘
```

---

## 2. Component Design & Responsibilities

### Service 1 — API Gateway (`backend/api-gateway`)
- Pure Java `HttpServer` with Virtual Threads.
- Reverse proxy logic in `ProxyHandler` supporting streaming input/output buffers.
- Correlation Context injection (`X-Request-ID`, `X-Correlation-ID`) across all downstreams.
- Centralized CORS and error formatting (`ErrorResponseDTO`).

### Service 2 — Space Weather Simulation Service (`backend/simulation-service`)
- **Queue**: Thread-safe bounded `LinkedBlockingQueue` with a configurable worker pool.
- **RAG Pipeline**: Retrieves top-K high-scoring memory chunks before constructing the prompt.
- **LLM Integration**: Interacts with Ollama Llama 3.1:8B with structured JSON schema and fallback to `DeterministicScenarioGenerator`.
- **Semantic Chunker**: Breaks scenarios into 7 semantic categories: `EVENT`, `ENVIRONMENT`, `GEOGRAPHIC_IMPACT`, `SATELLITE_IMPACT`, `OPERATIONAL_IMPACT`, `RECOMMENDATION`, `FEEDBACK`.
- **Memory Store**: Vector store with cosine similarity ranking and feedback weighting.

### Service 3 — Main Backend (`backend/main-backend`)
- **Event Orchestration**: Validates incoming events, checks idempotency, stores records in MySQL.
- **Satellite Constellation**: Manages LEO, MEO, GEO, and HEO satellites with differential vulnerability profiles.
- **Hybrid Risk Engine**:
  - Deterministic formula evaluates physical factors (solar flare class, geomagnetic Kp, proton flux, solar wind velocity, orbit exposure) generating a 0-100 score.
  - LLM/RAG explainer contextualizes the score with physical causal factors and system impacts.
- **Decision Support Engine**: Synthesizes mission-critical directives with confidence ratings.
- **WebSocket Broadcast**: Dispatches real-time events on port 8085 to all connected clients.
