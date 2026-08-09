package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SimulationConfigDTO(
    int intervalSeconds,
    int workerCount,
    String mode,
    String defaultIntensity,
    boolean isActive,
    Instant updatedAt
) {}
