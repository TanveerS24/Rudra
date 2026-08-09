package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RiskAssessment {
    private final String assessmentId;
    private final String eventId;
    private final String satelliteId;
    private final double deterministicScore;
    private final double finalScore;
    private final RiskLevel riskLevel;
    private final List<String> primaryFactors;
    private final List<String> potentialEffects;
    private final Instant createdAt;

    @JsonCreator
    public RiskAssessment(
            @JsonProperty("assessmentId") String assessmentId,
            @JsonProperty("eventId") String eventId,
            @JsonProperty("satelliteId") String satelliteId,
            @JsonProperty("deterministicScore") double deterministicScore,
            @JsonProperty("finalScore") double finalScore,
            @JsonProperty("riskLevel") RiskLevel riskLevel,
            @JsonProperty("primaryFactors") List<String> primaryFactors,
            @JsonProperty("potentialEffects") List<String> potentialEffects,
            @JsonProperty("createdAt") Instant createdAt) {
        this.assessmentId = Objects.requireNonNull(assessmentId, "assessmentId cannot be null");
        this.eventId = Objects.requireNonNull(eventId, "eventId cannot be null");
        this.satelliteId = Objects.requireNonNull(satelliteId, "satelliteId cannot be null");
        this.deterministicScore = Math.max(0.0, Math.min(100.0, deterministicScore));
        this.finalScore = Math.max(0.0, Math.min(100.0, finalScore));
        this.riskLevel = riskLevel != null ? riskLevel : RiskLevel.fromScore(this.finalScore);
        this.primaryFactors = primaryFactors != null ? Collections.unmodifiableList(new ArrayList<>(primaryFactors)) : Collections.emptyList();
        this.potentialEffects = potentialEffects != null ? Collections.unmodifiableList(new ArrayList<>(potentialEffects)) : Collections.emptyList();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getAssessmentId() { return assessmentId; }
    public String getEventId() { return eventId; }
    public String getSatelliteId() { return satelliteId; }
    public double getDeterministicScore() { return deterministicScore; }
    public double getFinalScore() { return finalScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public List<String> getPrimaryFactors() { return primaryFactors; }
    public List<String> getPotentialEffects() { return potentialEffects; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskAssessment that = (RiskAssessment) o;
        return Objects.equals(assessmentId, that.assessmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assessmentId);
    }

    @Override
    public String toString() {
        return "RiskAssessment{" +
                "id='" + assessmentId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", satId='" + satelliteId + '\'' +
                ", score=" + finalScore +
                ", level=" + riskLevel +
                '}';
    }
}
