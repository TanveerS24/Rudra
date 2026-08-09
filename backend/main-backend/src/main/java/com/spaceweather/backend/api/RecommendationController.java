package com.spaceweather.backend.api;

import com.spaceweather.backend.application.SpaceWeatherService;
import com.spaceweather.backend.persistence.RecommendationRepository;
import com.spaceweather.shared.dto.RecommendationDTO;
import com.spaceweather.shared.error.NotFoundException;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RecommendationController implements HttpHandler {
    private final RecommendationRepository repo;
    private final SpaceWeatherService spaceWeatherService;

    public RecommendationController(RecommendationRepository repo, SpaceWeatherService spaceWeatherService) {
        this.repo = repo;
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
                List<RecommendationDTO> list = repo.findAll(limit).stream()
                        .map(spaceWeatherService::toRecommendationDTO).toList();
                HttpUtils.sendJsonResponse(exchange, 200, list);
            } else if ("PUT".equals(method) && path.matches(".*/recommendations/[^/]+/status$")) {
                String[] parts = path.split("/");
                String recId = parts[parts.length - 2];
                Map<?, ?> body = HttpUtils.readJsonBody(exchange, Map.class);
                String status = (String) body.get("status");
                boolean updated = repo.updateStatus(recId, status != null ? status : "EXECUTED");
                if (!updated) {
                    throw new NotFoundException("Recommendation not found: " + recId);
                }
                HttpUtils.sendJsonResponse(exchange, 200, Map.of("updated", true, "recommendationId", recId, "status", status));
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
