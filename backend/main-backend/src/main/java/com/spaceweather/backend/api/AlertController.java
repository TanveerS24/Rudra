package com.spaceweather.backend.api;

import com.spaceweather.backend.application.SpaceWeatherService;
import com.spaceweather.backend.persistence.AlertRepository;
import com.spaceweather.shared.dto.AlertDTO;
import com.spaceweather.shared.error.NotFoundException;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AlertController implements HttpHandler {
    private final AlertRepository alertRepository;
    private final SpaceWeatherService spaceWeatherService;

    public AlertController(AlertRepository alertRepository, SpaceWeatherService spaceWeatherService) {
        this.alertRepository = alertRepository;
        this.spaceWeatherService = spaceWeatherService;
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
            if ("GET".equals(method)) {
                Map<String, String> query = HttpUtils.parseQueryParams(exchange.getRequestURI().getQuery());
                int limit = query.containsKey("limit") ? Integer.parseInt(query.get("limit")) : 20;
                List<AlertDTO> list = alertRepository.findAll(limit).stream()
                        .map(spaceWeatherService::toAlertDTO).toList();
                HttpUtils.sendJsonResponse(exchange, 200, list);
            } else if ("PUT".equals(method) && path.matches(".*/alerts/[^/]+/acknowledge$")) {
                String[] parts = path.split("/");
                String alertId = parts[parts.length - 2];
                boolean acknowledged = alertRepository.acknowledge(alertId);
                if (!acknowledged) {
                    throw new NotFoundException("Alert not found: " + alertId);
                }
                HttpUtils.sendJsonResponse(exchange, 200, Map.of("acknowledged", true, "alertId", alertId));
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
