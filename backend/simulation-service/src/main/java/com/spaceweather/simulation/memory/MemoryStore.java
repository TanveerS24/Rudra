package com.spaceweather.simulation.memory;

import com.spaceweather.shared.model.ChunkType;
import com.spaceweather.shared.model.MemoryChunk;

import java.util.List;
import java.util.Optional;

public interface MemoryStore {
    MemoryChunk store(MemoryChunk chunk);
    Optional<MemoryChunk> findById(String chunkId);
    List<MemoryChunk> retrieveSimilar(String query, int topK);
    List<MemoryChunk> retrieveByType(ChunkType type, int limit);
    List<MemoryChunk> retrieveByEvent(String eventId);
    List<MemoryChunk> retrieveHighQuality(int limit);
    boolean updateFeedbackScore(String chunkId, double newScore);
    long count();
}
