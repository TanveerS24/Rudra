package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class Mission {
    private final String missionId;
    private final String name;
    private final String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private final String status;   // ACTIVE, PAUSED, COMPLETED
    private final String targetSatelliteId;
    private final Instant startTime;
    private final Instant endTime;
    private final Instant createdAt;

    @JsonCreator
    public Mission(
            @JsonProperty("missionId") String missionId,
            @JsonProperty("name") String name,
            @JsonProperty("priority") String priority,
            @JsonProperty("status") String status,
            @JsonProperty("targetSatelliteId") String targetSatelliteId,
            @JsonProperty("startTime") Instant startTime,
            @JsonProperty("endTime") Instant endTime,
            @JsonProperty("createdAt") Instant createdAt) {
        this.missionId = Objects.requireNonNull(missionId, "missionId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.priority = priority != null ? priority : "MEDIUM";
        this.status = status != null ? status : "ACTIVE";
        this.targetSatelliteId = targetSatelliteId;
        this.startTime = startTime != null ? startTime : Instant.now();
        this.endTime = endTime;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getMissionId() { return missionId; }
    public String getName() { return name; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getTargetSatelliteId() { return targetSatelliteId; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mission mission = (Mission) o;
        return Objects.equals(missionId, mission.missionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(missionId);
    }
}
