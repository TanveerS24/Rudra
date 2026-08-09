package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SpaceWeatherEvent {
    private final String eventId;
    private final Instant timestamp;
    private final EventType eventType;
    private final String intensity;
    private final int durationMinutes;
    private final double solarWindSpeed;
    private final int geomagneticIndex;
    private final String radiationLevel;
    private final Location origin;
    private final List<String> affectedRegions;
    private final Location maximumImpactLocation;
    private final String impactDescription;
    private final double confidence;

    @JsonCreator
    public SpaceWeatherEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("intensity") String intensity,
            @JsonProperty("durationMinutes") int durationMinutes,
            @JsonProperty("solarWindSpeed") double solarWindSpeed,
            @JsonProperty("geomagneticIndex") int geomagneticIndex,
            @JsonProperty("radiationLevel") String radiationLevel,
            @JsonProperty("origin") Location origin,
            @JsonProperty("affectedRegions") List<String> affectedRegions,
            @JsonProperty("maximumImpactLocation") Location maximumImpactLocation,
            @JsonProperty("impactDescription") String impactDescription,
            @JsonProperty("confidence") double confidence) {
        this.eventId = Objects.requireNonNull(eventId, "eventId cannot be null");
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.eventType = Objects.requireNonNull(eventType, "eventType cannot be null");
        this.intensity = Objects.requireNonNull(intensity, "intensity cannot be null");
        this.durationMinutes = Math.max(1, durationMinutes);
        this.solarWindSpeed = Math.max(0.0, solarWindSpeed);
        this.geomagneticIndex = Math.max(0, Math.min(9, geomagneticIndex));
        this.radiationLevel = radiationLevel != null ? radiationLevel : "NORMAL";
        this.origin = origin != null ? origin : new Location(0.0, 0.0);
        this.affectedRegions = affectedRegions != null ? Collections.unmodifiableList(new ArrayList<>(affectedRegions)) : Collections.emptyList();
        this.maximumImpactLocation = maximumImpactLocation != null ? maximumImpactLocation : new Location(0.0, 0.0);
        this.impactDescription = impactDescription != null ? impactDescription : "";
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }

    public String getEventId() { return eventId; }
    public Instant getTimestamp() { return timestamp; }
    public EventType getEventType() { return eventType; }
    public String getIntensity() { return intensity; }
    public int getDurationMinutes() { return durationMinutes; }
    public double getSolarWindSpeed() { return solarWindSpeed; }
    public int getGeomagneticIndex() { return geomagneticIndex; }
    public String getRadiationLevel() { return radiationLevel; }
    public Location getOrigin() { return origin; }
    public List<String> getAffectedRegions() { return affectedRegions; }
    public Location getMaximumImpactLocation() { return maximumImpactLocation; }
    public String getImpactDescription() { return impactDescription; }
    public double getConfidence() { return confidence; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpaceWeatherEvent that = (SpaceWeatherEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "SpaceWeatherEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType=" + eventType +
                ", intensity='" + intensity + '\'' +
                ", Kp=" + geomagneticIndex +
                ", solarWind=" + solarWindSpeed +
                '}';
    }
}
