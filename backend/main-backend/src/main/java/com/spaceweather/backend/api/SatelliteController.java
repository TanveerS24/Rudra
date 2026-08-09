package com.spaceweather.backend.api;

import com.spaceweather.backend.application.SatelliteService;
import com.spaceweather.shared.dto.SatelliteDTO;
import com.spaceweather.shared.error.NotFoundException;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SatelliteController implements HttpHandler {
    private final SatelliteService satelliteService;

    public SatelliteController(SatelliteService satelliteService) {
        this.satelliteService = satelliteService;
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
                if (path.matches(".*/satellites/[^/]+$")) {
                    String satId = path.substring(path.lastIndexOf('/') + 1);
                    Satellite sat = satelliteService.getSatelliteById(satId)
                            .orElseThrow(() -> new NotFoundException("Satellite not found: " + satId));
                    HttpUtils.sendJsonResponse(exchange, 200, satelliteService.toDTO(sat));
                } else {
                    List<SatelliteDTO> list = satelliteService.getAllSatellites().stream()
                            .map(satelliteService::toDTO).toList();
                    HttpUtils.sendJsonResponse(exchange, 200, list);
                }
            } else if ("PUT".equals(method) && path.matches(".*/satellites/[^/]+/status$")) {
                String[] parts = path.split("/");
                String satId = parts[parts.length - 2];
                Map<?, ?> body = HttpUtils.readJsonBody(exchange, Map.class);
                String health = (String) body.get("healthStatus");
                String status = (String) body.get("operationalStatus");
                boolean updated = satelliteService.updateSatelliteStatus(satId, health, status);
                if (!updated) {
                    throw new NotFoundException("Satellite not found: " + satId);
                }
                HttpUtils.sendJsonResponse(exchange, 200, Map.of("updated", true, "satelliteId", satId));
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
