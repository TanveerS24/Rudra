# RAG Pipeline, Semantic Chunking & Memory Architecture

## 1. Simulation RAG Pipeline

```text
Simulation Scheduler / Trigger
              ↓
    Asynchronous Queue
              ↓
  Persistent Memory Retrieval (Vector Search)
              ↓
  Semantic Context Prompt Construction
              ↓
   Llama 3.1:8B (or Fallback Generator)
              ↓
    Structured Scenario Generation
              ↓
    Schema & Boundary Validation
              ↓
     7-Category Semantic Chunking
              ↓
  Persistent Vector Memory Storage
              ↓
      Forward to Main Backend
```

---

## 2. Mandatory Semantic Chunker Categories

Rather than naive fixed-length text slicing, the system uses semantic chunking:

1. **`EVENT`**: Core event classification, flare class, intensity, duration, confidence.
2. **`ENVIRONMENT`**: Solar wind velocity, geomagnetic Kp index, proton radiation flux.
3. **`GEOGRAPHIC_IMPACT`**: Terrestrial latitude/longitude impact coordinates, affected subauroral zones.
4. **`SATELLITE_IMPACT`**: Constellation-specific orbit degradation, thermospheric drag, SEU risks.
5. **`OPERATIONAL_IMPACT`**: HF radio blackout margins, satellite navigation pseudo-range timing drift.
6. **`RECOMMENDATION`**: Action directives and physical justification.
7. **`FEEDBACK`**: Historical quality scores and accuracy feedback.

---

## 3. Dynamic Feedback-Weighted Memory Retrieval

The `VectorMemoryStore` computes cosine similarity over normalized vector embeddings and weights results using:

$$\text{FinalScore} = \text{CosineSimilarity}(Q, C) \times \text{Importance} \times (0.4 + 0.6 \times \text{FeedbackScore})$$

- High-scoring, validated memories receive higher ranking in future scenario generation.
- Low-scoring memories are naturally demoted.
