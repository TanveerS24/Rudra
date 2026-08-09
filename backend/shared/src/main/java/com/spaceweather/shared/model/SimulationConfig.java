package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class SimulationConfig {
    private int intervalSeconds;
    private int workerCount;
    private String mode; // HYBRID_LLM, DETERMINISTIC_ONLY, RAG_ASSISTED
    private String defaultIntensity; // LOW, MODERATE, HIGH, CRITICAL, RANDOM
    private boolean isActive;
    private Instant updatedAt;

    @JsonCreator
    public SimulationConfig(
            @JsonProperty("intervalSeconds") int intervalSeconds,
            @JsonProperty("workerCount") int workerCount,
            @JsonProperty("mode") String mode,
            @JsonProperty("defaultIntensity") String defaultIntensity,
            @JsonProperty("isActive") boolean isActive,
            @JsonProperty("updatedAt") Instant updatedAt) {
        this.intervalSeconds = Math.max(2, Math.min(300, intervalSeconds));
        this.workerCount = Math.max(1, Math.min(16, workerCount));
        this.mode = mode != null ? mode : "HYBRID_LLM";
        this.defaultIntensity = defaultIntensity != null ? defaultIntensity : "MODERATE";
        this.isActive = isActive;
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static SimulationConfig defaultConfiguration() {
        return new SimulationConfig(15, 2, "HYBRID_LLM", "MODERATE", true, Instant.now());
    }

    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = Math.max(2, Math.min(300, intervalSeconds)); }
    public int getWorkerCount() { return workerCount; }
    public void setWorkerCount(int workerCount) { this.workerCount = Math.max(1, Math.min(16, workerCount)); }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getDefaultIntensity() { return defaultIntensity; }
    public void setDefaultIntensity(String defaultIntensity) { this.defaultIntensity = defaultIntensity; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
