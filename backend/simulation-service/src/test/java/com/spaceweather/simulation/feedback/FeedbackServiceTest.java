package com.spaceweather.simulation.feedback;

import com.spaceweather.simulation.chunking.SpaceWeatherSemanticChunker;
import com.spaceweather.simulation.config.SimulationServiceConfig;
import com.spaceweather.simulation.memory.EmbeddingService;
import com.spaceweather.simulation.memory.VectorMemoryStore;
import com.spaceweather.shared.model.FeedbackScore;
import com.spaceweather.shared.model.MemoryChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackServiceTest {
    private FeedbackService feedbackService;
    private VectorMemoryStore memoryStore;

    @BeforeEach
    void setUp() {
        EmbeddingService embeddingService = new EmbeddingService(new SimulationServiceConfig());
        memoryStore = new VectorMemoryStore(embeddingService);
        SpaceWeatherSemanticChunker chunker = new SpaceWeatherSemanticChunker(embeddingService);
        feedbackService = new FeedbackService(memoryStore, chunker);
    }

    @Test
    @DisplayName("Submitting feedback score should adjust associated memories in store")
    void testFeedbackSubmission() {
        FeedbackScore score = feedbackService.submitFeedback("EVT-HIST-001", "EVENT", 0.95, 0.90, "Highly accurate prediction");
        assertNotNull(score);
        assertEquals(0.925, score.getCompositeScore(), 0.001);

        List<MemoryChunk> related = memoryStore.retrieveByEvent("EVT-HIST-001");
        assertFalse(related.isEmpty());
        for (MemoryChunk c : related) {
            assertTrue(c.getFeedbackScore() >= 0.85);
        }
    }
}
