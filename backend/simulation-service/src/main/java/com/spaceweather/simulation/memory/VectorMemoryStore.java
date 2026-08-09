package com.spaceweather.simulation.memory;

import com.spaceweather.shared.model.ChunkType;
import com.spaceweather.shared.model.MemoryChunk;
import com.spaceweather.shared.util.StructuredLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VectorMemoryStore implements MemoryStore {
    private static final StructuredLogger log = StructuredLogger.of(VectorMemoryStore.class, "SIMULATION-SERVICE");
    private final Map<String, MemoryChunk> storage = new ConcurrentHashMap<>();
    private final EmbeddingService embeddingService;

    public VectorMemoryStore(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
        seedHistoricalMemories();
    }

    private void seedHistoricalMemories() {
        List<MemoryChunk> seeds = List.of(
                new MemoryChunk(
                        "CHK-SEED-01", "EVT-HIST-001", ChunkType.EVENT,
                        "Severe X2.4-class solar flare accompanied by CME and strong radio blackout.",
                        1.2, 0.92, embeddingService.computeEmbedding("Severe X2.4-class solar flare accompanied by CME and strong radio blackout."),
                        java.time.Instant.now()
                ),
                new MemoryChunk(
                        "CHK-SEED-02", "EVT-HIST-001", ChunkType.ENVIRONMENT,
                        "Solar wind velocity peaked at 620 km/s with planetary Kp index reaching 7.4.",
                        1.0, 0.88, embeddingService.computeEmbedding("Solar wind velocity peaked at 620 km/s with planetary Kp index reaching 7.4."),
                        java.time.Instant.now()
                ),
                new MemoryChunk(
                        "CHK-SEED-03", "EVT-HIST-001", ChunkType.SATELLITE_IMPACT,
                        "High radiation conditions increase single-event upset probability on polar LEO crossing orbits.",
                        1.5, 0.95, embeddingService.computeEmbedding("High radiation conditions increase single-event upset probability on polar LEO crossing orbits."),
                        java.time.Instant.now()
                ),
                new MemoryChunk(
                        "CHK-SEED-04", "EVT-HIST-001", ChunkType.RECOMMENDATION,
                        "Orient planar solar arrays to knife-edge profile and suspend extravehicular activities when risk exceeds 75.",
                        1.8, 0.96, embeddingService.computeEmbedding("Orient planar solar arrays to knife-edge profile and suspend extravehicular activities when risk exceeds 75."),
                        java.time.Instant.now()
                )
        );
        for (MemoryChunk c : seeds) {
            storage.put(c.getChunkId(), c);
        }
    }

    @Override
    public MemoryChunk store(MemoryChunk chunk) {
        Objects.requireNonNull(chunk, "chunk cannot be null");
        storage.put(chunk.getChunkId(), chunk);
        log.debug("Stored memory chunk: {} (Type: {}, Feedback: {})", chunk.getChunkId(), chunk.getChunkType(), chunk.getFeedbackScore());
        return chunk;
    }

    @Override
    public Optional<MemoryChunk> findById(String chunkId) {
        if (chunkId == null) return Optional.empty();
        return Optional.ofNullable(storage.get(chunkId));
    }

    @Override
    public List<MemoryChunk> retrieveSimilar(String query, int topK) {
        if (query == null || query.isBlank() || storage.isEmpty()) {
            return Collections.emptyList();
        }

        List<Double> queryVec = embeddingService.computeEmbedding(query);
        record ScoredChunk(MemoryChunk chunk, double weightedScore) {}

        List<ScoredChunk> scoredList = new ArrayList<>();

        for (MemoryChunk chunk : storage.values()) {
            double sim = EmbeddingService.cosineSimilarity(queryVec, chunk.getEmbedding());
            // Weight similarity by feedback score (0.4 base + 0.6 feedback) and importance
            double feedbackWeight = 0.4 + (0.6 * chunk.getFeedbackScore());
            double weightedScore = sim * feedbackWeight * chunk.getImportance();

            if (weightedScore > 0.05) {
                scoredList.add(new ScoredChunk(chunk, weightedScore));
            }
        }

        scoredList.sort((a, b) -> Double.compare(b.weightedScore, a.weightedScore));

        List<MemoryChunk> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scoredList.size()); i++) {
            results.add(scoredList.get(i).chunk);
        }
        return results;
    }

    @Override
    public List<MemoryChunk> retrieveByType(ChunkType type, int limit) {
        if (type == null) return Collections.emptyList();
        List<MemoryChunk> list = new ArrayList<>();
        for (MemoryChunk c : storage.values()) {
            if (c.getChunkType() == type) {
                list.add(c);
            }
        }
        list.sort((a, b) -> Double.compare(b.getFeedbackScore(), a.getFeedbackScore()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    @Override
    public List<MemoryChunk> retrieveByEvent(String eventId) {
        if (eventId == null) return Collections.emptyList();
        List<MemoryChunk> list = new ArrayList<>();
        for (MemoryChunk c : storage.values()) {
            if (eventId.equals(c.getSourceEventId())) {
                list.add(c);
            }
        }
        return list;
    }

    @Override
    public List<MemoryChunk> retrieveHighQuality(int limit) {
        List<MemoryChunk> list = new ArrayList<>(storage.values());
        list.sort((a, b) -> Double.compare(b.getFeedbackScore() * b.getImportance(), a.getFeedbackScore() * a.getImportance()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    @Override
    public boolean updateFeedbackScore(String chunkId, double newScore) {
        MemoryChunk chunk = storage.get(chunkId);
        if (chunk != null) {
            chunk.setFeedbackScore(Math.max(0.0, Math.min(1.0, newScore)));
            log.info("Updated feedback score for chunk {}: {}", chunkId, newScore);
            return true;
        }
        return false;
    }

    @Override
    public long count() {
        return storage.size();
    }
}
