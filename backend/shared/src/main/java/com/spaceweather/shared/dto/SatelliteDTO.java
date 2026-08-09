package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SatelliteDTO(
    String satelliteId,
    String name,
    String missionType,
    String orbitType,
    double altitudeKm,
    double inclinationDeg,
    double latitude,
    double longitude,
    String healthStatus,
    String radiationSensitivity,
    String communicationSensitivity,
    String navigationSensitivity,
    String operationalStatus,
    Instant createdAt,
    Instant updatedAt
) {}
