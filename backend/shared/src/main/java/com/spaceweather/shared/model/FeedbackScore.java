package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class FeedbackScore {
    private final String scoreId;
    private final String targetId;
    private final String targetType; // EVENT, RECOMMENDATION, RISK_ASSESSMENT
    private final double accuracyScore;
    private final double usefulnessScore;
    private final String comments;
    private final Instant createdAt;

    @JsonCreator
    public FeedbackScore(
            @JsonProperty("scoreId") String scoreId,
            @JsonProperty("targetId") String targetId,
            @JsonProperty("targetType") String targetType,
            @JsonProperty("accuracyScore") double accuracyScore,
            @JsonProperty("usefulnessScore") double usefulnessScore,
            @JsonProperty("comments") String comments,
            @JsonProperty("createdAt") Instant createdAt) {
        this.scoreId = Objects.requireNonNull(scoreId, "scoreId cannot be null");
        this.targetId = Objects.requireNonNull(targetId, "targetId cannot be null");
        this.targetType = Objects.requireNonNull(targetType, "targetType cannot be null");
        this.accuracyScore = Math.max(0.0, Math.min(1.0, accuracyScore));
        this.usefulnessScore = Math.max(0.0, Math.min(1.0, usefulnessScore));
        this.comments = comments != null ? comments : "";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getScoreId() { return scoreId; }
    public String getTargetId() { return targetId; }
    public String getTargetType() { return targetType; }
    public double getAccuracyScore() { return accuracyScore; }
    public double getUsefulnessScore() { return usefulnessScore; }
    public double getCompositeScore() { return (accuracyScore * 0.5) + (usefulnessScore * 0.5); }
    public String getComments() { return comments; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackScore that = (FeedbackScore) o;
        return Objects.equals(scoreId, that.scoreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scoreId);
    }
}
