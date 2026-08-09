package com.spaceweather.backend.api;

import com.spaceweather.backend.application.SpaceWeatherService;
import com.spaceweather.backend.persistence.RiskAssessmentRepository;
import com.spaceweather.backend.persistence.SatelliteRepository;
import com.spaceweather.shared.dto.RiskAssessmentDTO;
import com.spaceweather.shared.dto.SpaceWeatherEventDTO;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HistoryController implements HttpHandler {
    private final SpaceWeatherService spaceWeatherService;
    private final RiskAssessmentRepository riskRepo;
    private final SatelliteRepository satRepo;

    public HistoryController(SpaceWeatherService spaceWeatherService, RiskAssessmentRepository riskRepo, SatelliteRepository satRepo) {
        this.spaceWeatherService = spaceWeatherService;
        this.riskRepo = riskRepo;
        this.satRepo = satRepo;
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
        Map<String, String> query = HttpUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        int limit = query.containsKey("limit") ? Integer.parseInt(query.get("limit")) : 50;

        try {
            if ("GET".equals(method)) {
                if (path.endsWith("/events")) {
                    List<SpaceWeatherEventDTO> list = spaceWeatherService.getRecentEvents(limit).stream()
                            .map(spaceWeatherService::toEventDTO).toList();
                    HttpUtils.sendJsonResponse(exchange, 200, list);
                } else if (path.endsWith("/risk")) {
                    List<Satellite> satellites = satRepo.findAll();
                    List<RiskAssessmentDTO> list = riskRepo.findRecent(limit).stream()
                            .map(r -> spaceWeatherService.toRiskAssessmentDTO(r, satellites)).toList();
                    HttpUtils.sendJsonResponse(exchange, 200, list);
                } else {
                    exchange.sendResponseHeaders(404, -1);
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
