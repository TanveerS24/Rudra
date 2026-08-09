package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class Recommendation {
    private final String recommendationId;
    private final String eventId;
    private final String assessmentId;
    private final String satelliteId;
    private final String action;
    private final String reasoning;
    private final String expectedImpact;
    private final double confidence;
    private String status; // PENDING, EXECUTED, DISMISSED
    private final Instant createdAt;

    @JsonCreator
    public Recommendation(
            @JsonProperty("recommendationId") String recommendationId,
            @JsonProperty("eventId") String eventId,
            @JsonProperty("assessmentId") String assessmentId,
            @JsonProperty("satelliteId") String satelliteId,
            @JsonProperty("action") String action,
            @JsonProperty("reasoning") String reasoning,
            @JsonProperty("expectedImpact") String expectedImpact,
            @JsonProperty("confidence") double confidence,
            @JsonProperty("status") String status,
            @JsonProperty("createdAt") Instant createdAt) {
        this.recommendationId = Objects.requireNonNull(recommendationId, "recommendationId cannot be null");
        this.eventId = Objects.requireNonNull(eventId, "eventId cannot be null");
        this.assessmentId = assessmentId;
        this.satelliteId = satelliteId;
        this.action = Objects.requireNonNull(action, "action cannot be null");
        this.reasoning = Objects.requireNonNull(reasoning, "reasoning cannot be null");
        this.expectedImpact = expectedImpact != null ? expectedImpact : "";
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        this.status = status != null ? status : "PENDING";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getRecommendationId() { return recommendationId; }
    public String getEventId() { return eventId; }
    public String getAssessmentId() { return assessmentId; }
    public String getSatelliteId() { return satelliteId; }
    public String getAction() { return action; }
    public String getReasoning() { return reasoning; }
    public String getExpectedImpact() { return expectedImpact; }
    public double getConfidence() { return confidence; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recommendation that = (Recommendation) o;
        return Objects.equals(recommendationId, that.recommendationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recommendationId);
    }

    @Override
    public String toString() {
        return "Recommendation{" +
                "id='" + recommendationId + '\'' +
                ", action='" + action + '\'' +
                ", confidence=" + confidence +
                ", status='" + status + '\'' +
                '}';
    }
}
