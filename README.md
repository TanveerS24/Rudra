# Integrated Space Weather Monitoring and Satellite Operations Decision Support System

An enterprise-grade, distributed mission control platform combining space weather simulation, satellite constellation vulnerability modeling, hybrid deterministic + LLM risk evaluation, AI decision support recommendations, persistent semantic memory with feedback reinforcement, and an interactive 3D space operations dashboard.

---

## Key Highlights

- **Pure Java Microservices (No Spring / No Hibernate)**:
  - **API Gateway (Port 8080)**: Reverse proxy, correlation ID propagation (`X-Request-ID`), structured logging, health aggregation.
  - **Main Backend (Port 8081 & WS 8085)**: Idempotent event processing, satellite fleet vulnerability modeling, hybrid deterministic + LLM risk engine, decision support directives, MySQL persistence (HikariCP), and WebSocket broadcasting.
  - **Simulation Service (Port 8082)**: Asynchronous worker queue, Ollama Llama 3.1:8B integration, 7-category Semantic Chunker, Persistent Vector Memory Store with cosine similarity, and feedback scoring.
  - **Shared Module**: Strongly typed Domain Models, DTOs, Exception hierarchy, Jackson JSON serialization, and structured logger.
- **Mission Control 3D React Frontend**:
  - Interactive 3D Earth with dynamic pulsing terrestrial plasma shockwaves colored by threat severity (LOW: Cyan, MODERATE: Amber, HIGH: Orange, CRITICAL: Pulsing Neon Red).
  - Virtual satellite orbit tracks (LEO, MEO, GEO, HEO) with 3D nodes, health beacons, and click-to-inspect HUD popups.
  - Telemetry HUD with real-time solar wind gauges, Kp Geomagnetic Index dial, Radiation flux meter, and risk dials.
  - Operational decision support cards with "Why" reasoning, expected outcomes, and directive execution controls.
  - Historical multi-metric synchronized telemetry area/line charts.
  - Dedicated `/simulator` control deck for configuring backend simulation frequency, queue size, workers, scenario intensity, and RAG memory introspection.
- **Docker Infrastructure**:
  - `docker-compose.yml` for API Gateway, Main Backend, Simulation Service, MySQL 8.0, and Ollama.
  - Parameterized via `.env`.

---

## System Architecture

```text
React 3D Frontend (Port 3000)
         │  REST & WebSockets
         ▼
API Gateway (Port 8080)
    ├── /api/v1/simulator/* ──► Simulation Service (Port 8082)
    │                                ├── Bounded Queue & Worker Pool
    │                                ├── Ollama (Llama 3.1:8B)
    │                                ├── 7-Category Semantic Chunker
    │                                └── Persistent Vector Memory Store
    │
    └── /api/v1/* ────────────► Main Backend (Port 8081 & WS 8085)
                                     ├── MySQL 8.0 Persistence (HikariCP)
                                     ├── Satellite Constellation Engine
                                     ├── Hybrid Risk Engine (Physics + LLM)
                                     ├── Decision Support Engine
                                     └── WebSocket Telemetry Broadcaster
```

---

## Quick Start Guide

### 1. Prerequisites
- Java 21 LTS
- Maven 3.9+
- Node.js 18+ & npm
- Docker & Docker Compose (Optional for containerized run)
- Ollama (Optional for local Llama 3.1:8B inference; automatic fallback provided)

### 2. Build & Test Backend
```bash
cd backend
mvn clean package
```

### 3. Run Backend Microservices

**Terminal 1 — Main Backend:**
```bash
java -jar backend/main-backend/target/main-backend-1.0.0.jar
```

**Terminal 2 — Simulation Service:**
```bash
java -jar backend/simulation-service/target/simulation-service-1.0.0.jar
```

**Terminal 3 — API Gateway:**
```bash
java -jar backend/api-gateway/target/api-gateway-1.0.0.jar
```

### 4. Run Frontend Mission Control
```bash
cd frontend
npm install
npm run dev
```
Open **http://localhost:3000** in your browser.

---

## Documentation Index

- [Architecture Overview](ARCHITECTURE.md)
- [REST & WebSocket API Specification](API.md)
- [Database Schema & Migrations](DATABASE.md)
- [RAG Pipeline, Semantic Chunking & Memory](RAG.md)
- [Simulation Service & Queue Mechanics](SIMULATION.md)
- [Docker & Production Deployment](DEPLOYMENT.md)
- [Observability, Tracing & Debugging](DEBUGGING.md)
