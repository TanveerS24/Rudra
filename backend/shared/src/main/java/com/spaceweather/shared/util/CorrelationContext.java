package com.spaceweather.shared.util;

import java.util.UUID;

public final class CorrelationContext {
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private CorrelationContext() {}

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId != null && !requestId.isBlank() ? requestId : "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static String getRequestId() {
        String id = REQUEST_ID.get();
        if (id == null) {
            id = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            REQUEST_ID.set(id);
        }
        return id;
    }

    public static void setCorrelationId(String correlationId) {
        CORRELATION_ID.set(correlationId != null && !correlationId.isBlank() ? correlationId : "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static String getCorrelationId() {
        String id = CORRELATION_ID.get();
        if (id == null) {
            id = "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            CORRELATION_ID.set(id);
        }
        return id;
    }

    public static void clear() {
        REQUEST_ID.remove();
        CORRELATION_ID.remove();
    }
}
