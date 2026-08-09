package com.spaceweather.gateway.routing;

import com.spaceweather.gateway.config.GatewayConfig;
import com.spaceweather.shared.dto.HealthStatusDTO;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class HealthCheckHandler implements HttpHandler {
    private final GatewayConfig config;
    private final HttpClient httpClient;

    public HealthCheckHandler(GatewayConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorrelationContext.setRequestId(exchange.getRequestHeaders().getFirst("X-Request-ID"));
        HttpUtils.setCorsHeaders(exchange);

        Map<String, Object> services = new HashMap<>();
        services.put("api-gateway", "UP");
        services.put("main-backend", checkService(config.getMainBackendUrl() + "/health"));
        services.put("simulation-service", checkService(config.getSimulationServiceUrl() + "/health"));

        boolean allHealthy = services.values().stream().noneMatch("DOWN"::equals);
        String overall = allHealthy ? "UP" : "DEGRADED";

        HealthStatusDTO health = new HealthStatusDTO(
                "api-gateway",
                overall,
                Instant.now(),
                services
        );

        HttpUtils.sendJsonResponse(exchange, allHealthy ? 200 : 503, health);
        CorrelationContext.clear();
    }

    private String checkService(String healthUrl) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<Void> resp = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            return resp.statusCode() == 200 ? "UP" : "DEGRADED";
        } catch (Exception e) {
            return "DOWN";
        }
    }
}
