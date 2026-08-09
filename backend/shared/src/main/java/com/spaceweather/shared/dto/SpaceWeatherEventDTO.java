package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpaceWeatherEventDTO(
    String eventId,
    Instant timestamp,
    String eventType,
    String intensity,
    int durationMinutes,
    double solarWindSpeed,
    int geomagneticIndex,
    String radiationLevel,
    LocationDTO origin,
    List<String> affectedRegions,
    LocationDTO maximumImpactLocation,
    String impactDescription,
    double confidence
) {
    public record LocationDTO(double latitude, double longitude) {}
}
