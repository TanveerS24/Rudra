package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HealthStatusDTO(
    String service,
    String status, // UP, DOWN, DEGRADED
    Instant timestamp,
    Map<String, Object> details
) {}
