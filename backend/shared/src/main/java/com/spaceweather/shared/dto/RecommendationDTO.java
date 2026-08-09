package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationDTO(
    String recommendationId,
    String eventId,
    String assessmentId,
    String satelliteId,
    String action,
    String reasoning,
    String expectedImpact,
    double confidence,
    String status,
    Instant createdAt
) {}
