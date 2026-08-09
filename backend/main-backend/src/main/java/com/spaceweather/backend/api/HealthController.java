package com.spaceweather.backend.api;

import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.shared.dto.HealthStatusDTO;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class HealthController implements HttpHandler {
    private final DatabaseConnectionPool dbPool;

    public HealthController(DatabaseConnectionPool dbPool) {
        this.dbPool = dbPool;
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
            boolean dbConnected = dbPool.isConnected();
            String status = "UP";
            Map<String, Object> details = Map.of(
                    "database", dbConnected ? "CONNECTED" : "IN_MEMORY_FALLBACK",
                    "threads", Thread.activeCount(),
                    "heapUsedMB", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
            );

            HealthStatusDTO health = new HealthStatusDTO("main-backend", status, Instant.now(), details);
            HttpUtils.sendJsonResponse(exchange, 200, health);
        } catch (Throwable t) {
            HttpUtils.sendErrorResponse(exchange, t, "MAIN-BACKEND");
        } finally {
            CorrelationContext.clear();
        }
    }
}
