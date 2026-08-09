package com.spaceweather.simulation.feedback;

import com.spaceweather.simulation.chunking.SemanticChunker;
import com.spaceweather.simulation.memory.MemoryStore;
import com.spaceweather.shared.dto.FeedbackScoreDTO;
import com.spaceweather.shared.model.FeedbackScore;
import com.spaceweather.shared.model.MemoryChunk;
import com.spaceweather.shared.util.StructuredLogger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FeedbackService {
    private static final StructuredLogger log = StructuredLogger.of(FeedbackService.class, "SIMULATION-SERVICE");
    private final Map<String, FeedbackScore> scores = new ConcurrentHashMap<>();
    private final MemoryStore memoryStore;
    private final SemanticChunker semanticChunker;

    public FeedbackService(MemoryStore memoryStore, SemanticChunker semanticChunker) {
        this.memoryStore = memoryStore;
        this.semanticChunker = semanticChunker;
    }

    public FeedbackScore submitFeedback(String targetId, String targetType, double accuracy, double usefulness, String comments) {
        String scoreId = "FBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        FeedbackScore score = new FeedbackScore(scoreId, targetId, targetType, accuracy, usefulness, comments, Instant.now());
        scores.put(scoreId, score);

        // 1. Create and store a FEEDBACK semantic chunk
        MemoryChunk feedbackChunk = semanticChunker.chunkFeedback(targetId, score.getCompositeScore(), comments);
        memoryStore.store(feedbackChunk);

        // 2. Update memory chunks associated with this target event
        List<MemoryChunk> related = memoryStore.retrieveByEvent(targetId);
        for (MemoryChunk c : related) {
            double current = c.getFeedbackScore();
            double updated = (current * 0.5) + (score.getCompositeScore() * 0.5);
            memoryStore.updateFeedbackScore(c.getChunkId(), updated);
        }

        log.info("Recorded feedback score for {} [{}]: accuracy={}, usefulness={}, composite={}",
                targetId, targetType, accuracy, usefulness, score.getCompositeScore());

        return score;
    }

    public List<FeedbackScoreDTO> getRecentScores(int limit) {
        return scores.values().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .map(this::toDTO)
                .toList();
    }

    public FeedbackScoreDTO toDTO(FeedbackScore fs) {
        return new FeedbackScoreDTO(
                fs.getScoreId(),
                fs.getTargetId(),
                fs.getTargetType(),
                fs.getAccuracyScore(),
                fs.getUsefulnessScore(),
                fs.getCompositeScore(),
                fs.getComments(),
                fs.getCreatedAt()
        );
    }
}
