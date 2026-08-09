package com.spaceweather.backend.api;

import com.spaceweather.backend.application.SpaceWeatherService;
import com.spaceweather.backend.persistence.SatelliteRepository;
import com.spaceweather.shared.dto.SpaceWeatherEventDTO;
import com.spaceweather.shared.error.NotFoundException;
import com.spaceweather.shared.model.EventType;
import com.spaceweather.shared.model.Location;
import com.spaceweather.shared.model.SpaceWeatherEvent;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class EventController implements HttpHandler {
    private final SpaceWeatherService service;
    private final SatelliteRepository satelliteRepository;

    public EventController(SpaceWeatherService service, SatelliteRepository satelliteRepository) {
        this.service = service;
        this.satelliteRepository = satelliteRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorrelationContext.setRequestId(exchange.getRequestHeaders().getFirst("X-Request-ID"));
        HttpUtils.setCorsHeaders(exchange);

        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method)) {
                // Ingest new event
                SpaceWeatherEventDTO dto = HttpUtils.readJsonBody(exchange, SpaceWeatherEventDTO.class);
                SpaceWeatherEvent event = new SpaceWeatherEvent(
                        dto.eventId(),
                        dto.timestamp() != null ? dto.timestamp() : Instant.now(),
                        dto.eventType() != null ? EventType.valueOf(dto.eventType().toUpperCase()) : EventType.SOLAR_FLARE,
                        dto.intensity() != null ? dto.intensity() : "M1.0",
                        dto.durationMinutes() > 0 ? dto.durationMinutes() : 30,
                        dto.solarWindSpeed() > 0 ? dto.solarWindSpeed() : 450.0,
                        dto.geomagneticIndex() >= 0 ? dto.geomagneticIndex() : 3,
                        dto.radiationLevel() != null ? dto.radiationLevel() : "NORMAL",
                        dto.origin() != null ? new Location(dto.origin().latitude(), dto.origin().longitude()) : new Location(0.0, 0.0),
                        dto.affectedRegions(),
                        dto.maximumImpactLocation() != null ? new Location(dto.maximumImpactLocation().latitude(), dto.maximumImpactLocation().longitude()) : new Location(0.0, 0.0),
                        dto.impactDescription(),
                        dto.confidence() > 0 ? dto.confidence() : 0.85
                );
                SpaceWeatherEvent processed = service.processEvent(event);
                HttpUtils.sendJsonResponse(exchange, 201, service.toEventDTO(processed));
            } else if ("GET".equals(method)) {
                if (path.endsWith("/latest")) {
                    SpaceWeatherEvent latest = service.getLatestEvent()
                            .orElseThrow(() -> new NotFoundException("No space weather events found"));
                    HttpUtils.sendJsonResponse(exchange, 200, service.toEventDTO(latest));
                } else if (path.matches(".*/events/[^/]+$")) {
                    String eventId = path.substring(path.lastIndexOf('/') + 1);
                    SpaceWeatherEvent event = service.getEventById(eventId)
                            .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
                    HttpUtils.sendJsonResponse(exchange, 200, service.toEventDTO(event));
                } else {
                    Map<String, String> query = HttpUtils.parseQueryParams(exchange.getRequestURI().getQuery());
                    int limit = query.containsKey("limit") ? Integer.parseInt(query.get("limit")) : 20;
                    List<SpaceWeatherEventDTO> list = service.getRecentEvents(limit).stream()
                            .map(service::toEventDTO).toList();
                    HttpUtils.sendJsonResponse(exchange, 200, list);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (Throwable t) {
            HttpUtils.sendErrorResponse(exchange, t, "MAIN-BACKEND");
        } finally {
            CorrelationContext.clear();
        }
    }
}
