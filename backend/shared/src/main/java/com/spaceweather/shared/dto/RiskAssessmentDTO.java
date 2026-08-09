package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RiskAssessmentDTO(
    String assessmentId,
    String eventId,
    String satelliteId,
    String satelliteName,
    double deterministicScore,
    double finalScore,
    String riskLevel,
    List<String> primaryFactors,
    List<String> potentialEffects,
    Instant createdAt
) {}
