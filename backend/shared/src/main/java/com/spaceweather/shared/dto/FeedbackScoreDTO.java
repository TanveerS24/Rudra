package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FeedbackScoreDTO(
    String scoreId,
    String targetId,
    String targetType,
    double accuracyScore,
    double usefulnessScore,
    double compositeScore,
    String comments,
    Instant createdAt
) {}
