package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebSocketMessageDTO(
    String type, // SPACE_WEATHER_EVENT, RISK_UPDATED, ALERT_CREATED, SATELLITE_STATUS_CHANGED, RECOMMENDATION_CREATED, SIMULATION_STATUS_CHANGED, HEARTBEAT
    Instant timestamp,
    Object payload,
    String correlationId
) {
    public static WebSocketMessageDTO of(String type, Object payload, String correlationId) {
        return new WebSocketMessageDTO(type, Instant.now(), payload, correlationId);
    }
}
