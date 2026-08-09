package com.spaceweather.backend.api;

import com.spaceweather.backend.application.SpaceWeatherService;
import com.spaceweather.backend.persistence.RiskAssessmentRepository;
import com.spaceweather.backend.persistence.SatelliteRepository;
import com.spaceweather.shared.dto.RiskAssessmentDTO;
import com.spaceweather.shared.error.NotFoundException;
import com.spaceweather.shared.model.RiskAssessment;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RiskController implements HttpHandler {
    private final RiskAssessmentRepository riskRepo;
    private final SatelliteRepository satRepo;
    private final SpaceWeatherService spaceWeatherService;

    public RiskController(RiskAssessmentRepository riskRepo, SatelliteRepository satRepo, SpaceWeatherService spaceWeatherService) {
        this.riskRepo = riskRepo;
        this.satRepo = satRepo;
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
        List<Satellite> satellites = satRepo.findAll();

        try {
            if ("GET".equals(method)) {
                if (path.contains("/event/")) {
                    String eventId = path.substring(path.lastIndexOf('/') + 1);
                    List<RiskAssessmentDTO> list = riskRepo.findByEventId(eventId).stream()
                            .map(r -> spaceWeatherService.toRiskAssessmentDTO(r, satellites)).toList();
                    HttpUtils.sendJsonResponse(exchange, 200, list);
                } else if (path.matches(".*/risk/[^/]+$")) {
                    String assessmentId = path.substring(path.lastIndexOf('/') + 1);
                    RiskAssessment assessment = riskRepo.findById(assessmentId)
                            .orElseThrow(() -> new NotFoundException("Risk assessment not found: " + assessmentId));
                    HttpUtils.sendJsonResponse(exchange, 200, spaceWeatherService.toRiskAssessmentDTO(assessment, satellites));
                } else {
                    Map<String, String> query = HttpUtils.parseQueryParams(exchange.getRequestURI().getQuery());
                    int limit = query.containsKey("limit") ? Integer.parseInt(query.get("limit")) : 20;
                    List<RiskAssessmentDTO> list = riskRepo.findRecent(limit).stream()
                            .map(r -> spaceWeatherService.toRiskAssessmentDTO(r, satellites)).toList();
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
