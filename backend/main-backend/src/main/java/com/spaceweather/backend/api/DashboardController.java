package com.spaceweather.backend.api;

import com.spaceweather.backend.application.DashboardService;
import com.spaceweather.shared.dto.DashboardSummaryDTO;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class DashboardController implements HttpHandler {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
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

        try {
            if ("GET".equals(method)) {
                DashboardSummaryDTO summary = dashboardService.getDashboardSummary();
                HttpUtils.sendJsonResponse(exchange, 200, summary);
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
