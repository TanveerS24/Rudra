package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlertDTO(
    String alertId,
    String eventId,
    String severity,
    String title,
    String message,
    boolean acknowledged,
    Instant createdAt
) {}
