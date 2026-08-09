package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
    Instant timestamp,
    int status,
    String code,
    String message,
    String path,
    String requestId
) {}
