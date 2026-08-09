package com.spaceweather.simulation.memory;

import com.spaceweather.simulation.config.SimulationServiceConfig;
import com.spaceweather.shared.model.ChunkType;
import com.spaceweather.shared.model.MemoryChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VectorMemoryStoreTest {
    private VectorMemoryStore memoryStore;
    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(new SimulationServiceConfig());
        memoryStore = new VectorMemoryStore(embeddingService);
    }

    @Test
    @DisplayName("Should retrieve semantically relevant memories based on cosine similarity and feedback weighting")
    void testSimilarMemoryRetrieval() {
        String text1 = "Extreme solar flare event with severe geomagnetic storm and orbital drag on satellites.";
        MemoryChunk chunk1 = new MemoryChunk(
                "CHK-T1", "EVT-1", ChunkType.EVENT, text1,
                2.0, 1.0, embeddingService.computeEmbedding(text1), Instant.now()
        );
        memoryStore.store(chunk1);

        String text2 = "Nominal background solar radiation and calm planetary field conditions.";
        MemoryChunk chunk2 = new MemoryChunk(
                "CHK-T2", "EVT-2", ChunkType.ENVIRONMENT, text2,
                0.5, 0.20, embeddingService.computeEmbedding(text2), Instant.now()
        );
        memoryStore.store(chunk2);

        List<MemoryChunk> results = memoryStore.retrieveSimilar("Extreme solar flare event with severe geomagnetic storm", 3);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(c -> "CHK-T1".equals(c.getChunkId())));
    }

    @Test
    @DisplayName("Feedback score update should modify memory ranking weights")
    void testFeedbackScoreUpdate() {
        boolean updated = memoryStore.updateFeedbackScore("CHK-SEED-01", 0.99);
        assertTrue(updated);
        assertEquals(0.99, memoryStore.findById("CHK-SEED-01").get().getFeedbackScore(), 0.001);
    }
}
