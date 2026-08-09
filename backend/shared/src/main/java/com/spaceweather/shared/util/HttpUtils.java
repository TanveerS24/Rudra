package com.spaceweather.shared.util;

import com.spaceweather.shared.dto.ErrorResponseDTO;
import com.spaceweather.shared.error.AppException;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class HttpUtils {

    private HttpUtils() {}

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonUtils.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        setCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("X-Request-ID", CorrelationContext.getRequestId());
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
            os.flush();
        }
    }

    public static void sendErrorResponse(HttpExchange exchange, Throwable t, String serviceName) throws IOException {
        int status = 500;
        String code = "INTERNAL_SERVER_ERROR";
        String message = "An internal server error occurred.";

        if (t instanceof AppException ae) {
            status = ae.getStatusCode();
            code = ae.getErrorCode();
            message = ae.getMessage();
        } else if (t instanceof IllegalArgumentException) {
            status = 400;
            code = "INVALID_REQUEST";
            message = t.getMessage();
        }

        ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                Instant.now(),
                status,
                code,
                message,
                exchange.getRequestURI().getPath(),
                CorrelationContext.getRequestId()
        );

        sendJsonResponse(exchange, status, errorDTO);
    }

    public static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Request-ID, X-Correlation-ID");
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    public static <T> T readJsonBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        String body = readBody(exchange);
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        return JsonUtils.fromJson(body, clazz);
    }

    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0 && idx < pair.length() - 1) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            } else if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                params.put(key, "");
            }
        }
        return params;
    }
}
