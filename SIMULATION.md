# Simulation Service & Worker Queue Mechanics

## 1. Asynchronous Queue Architecture

The simulation service isolates long-running LLM and RAG generation from HTTP request handling:

```text
HTTP Request / Scheduler Tick
              ↓
  Bounded LinkedBlockingQueue (Capacity 500)
              ↓
  Fixed Worker Thread Pool (Configurable worker.count)
              ↓
  RAG Context Retrieval & LLM Generation
              ↓
  CompletableFuture Resolution & Event Dispatch
```

---

## 2. Worker Pool Configuration

Configured via environment variables:
- `SIMULATION_WORKER_COUNT=2` (1 to 16 threads)
- `SIMULATION_INTERVAL=15` (5 to 60 seconds)
- `SIMULATION_FALLBACK=true` (Automatic physics generator fallback if Ollama is offline)

---

## 3. Simulator Control Deck (`/simulator`)

The hidden simulator deck empowers mission operators to:
- Dynamically alter trigger frequencies and thread workers without restart.
- Inspect live queue backlog, processed count, and failure metrics.
- Manually inject single scenarios with targeted intensity profiles (CRITICAL, HIGH, MODERATE).
- Inspect RAG semantic memories with similarity and feedback scores.
- Submit feedback ratings directly influencing future scenario generation.
