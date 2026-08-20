package com.spaceweather.gateway.routing;

import com.spaceweather.gateway.config.GatewayConfig;
import com.spaceweather.shared.dto.ErrorResponseDTO;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.spaceweather.shared.util.JsonUtils;
import com.spaceweather.shared.util.StructuredLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ProxyHandler implements HttpHandler {
    private static final StructuredLogger log = StructuredLogger.of(ProxyHandler.class, "API-GATEWAY");
    private final GatewayConfig config;
    private final HttpClient httpClient;

    public ProxyHandler(GatewayConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startTime = System.currentTimeMillis();
        String reqId = exchange.getRequestHeaders().getFirst("X-Request-ID");
        CorrelationContext.setRequestId(reqId);
        HttpUtils.setCorsHeaders(exchange);

        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            CorrelationContext.clear();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String fullPathWithQuery = (query != null && !query.isBlank()) ? path + "?" + query : path;

        // Determine destination upstream service
        String targetBaseUrl;
        if (path.startsWith("/api/v1/simulator") || path.startsWith("/api/simulator")) {
            targetBaseUrl = config.getSimulationServiceUrl();
        } else {
            targetBaseUrl = config.getMainBackendUrl();
        }

        String targetUrl = targetBaseUrl + fullPathWithQuery;
        log.info("Proxying {} {} -> {}", method, path, targetUrl);

        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("X-Request-ID", CorrelationContext.getRequestId())
                    .header("X-Correlation-ID", CorrelationContext.getCorrelationId())
                    .timeout(Duration.ofSeconds(120));

            // Copy Content-Type if present
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType != null) {
                reqBuilder.header("Content-Type", contentType);
            }

            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
                reqBuilder.method(method, HttpRequest.BodyPublishers.ofByteArray(bodyBytes));
            } else if ("DELETE".equals(method)) {
                reqBuilder.DELETE();
            } else {
                reqBuilder.GET();
            }

            HttpResponse<InputStream> response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            int statusCode = response.statusCode();

            // Forward response headers
            HttpUtils.setCorsHeaders(exchange);
            for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                String key = header.getKey();
                if (!key.equalsIgnoreCase("Transfer-Encoding") && !key.equalsIgnoreCase("Content-Length")) {
                    for (String val : header.getValue()) {
                        exchange.getResponseHeaders().add(key, val);
                    }
                }
            }
            exchange.getResponseHeaders().set("X-Request-ID", CorrelationContext.getRequestId());

            byte[] responseBytes;
            try (InputStream is = response.body()) {
                responseBytes = is.readAllBytes();
            }

            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
                os.flush();
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed proxy {} {} status={} duration={}ms", method, path, statusCode, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Gateway error proxying to " + targetUrl + " duration=" + duration + "ms", e);

            ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                    Instant.now(),
                    502,
                    "BAD_GATEWAY",
                    "Upstream service unavailable: " + e.getMessage(),
                    path,
                    CorrelationContext.getRequestId()
            );
            byte[] errorBytes = JsonUtils.toJson(errorDTO).getBytes(StandardCharsets.UTF_8);
            HttpUtils.setCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("X-Request-ID", CorrelationContext.getRequestId());
            exchange.sendResponseHeaders(502, errorBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
        } finally {
            CorrelationContext.clear();
        }
    }
}
