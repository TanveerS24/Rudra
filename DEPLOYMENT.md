# Docker & Production Deployment Guide

## 1. Docker Compose Stack

Run the full backend infrastructure with Docker Compose:

```bash
docker-compose up -d --build
```

### Services Started:
1. `mysql`: MySQL 8.0 with automatic migration initialization and healthcheck on port `3306`.
2. `ollama`: Ollama local LLM runtime on port `11434`.
3. `main-backend`: Main Backend microservice on port `8081` & WebSocket on `8085`.
4. `simulation-service`: Simulation Service on port `8082`.
5. `api-gateway`: Unified API Gateway entry point on port `8080`.

---

## 2. Local Ollama LLM Model Setup

When running Ollama locally or in Docker:
```bash
ollama pull llama3.1:8b
```

If Ollama is not active, the system automatically runs with zero interruption using its built-in physics simulation engine and deterministic rule explainer.

---

## 3. Running Frontend Outside Docker

The React frontend runs natively on the host:
```bash
cd frontend
npm install
npm run dev
```
Navigate to **http://localhost:3000** to access the Mission Control Center.
