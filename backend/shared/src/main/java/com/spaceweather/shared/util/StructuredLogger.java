package com.spaceweather.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructuredLogger {
    private final Logger logger;
    private final String serviceName;

    public StructuredLogger(Class<?> clazz, String serviceName) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.serviceName = serviceName;
    }

    public static StructuredLogger of(Class<?> clazz, String serviceName) {
        return new StructuredLogger(clazz, serviceName);
    }

    public void info(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(formatMessage(message, null), args);
        }
    }

    public void infoWithEvent(String eventId, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(formatMessage(message, eventId), args);
        }
    }

    public void warn(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatMessage(message, null), args);
        }
    }

    public void error(String message, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error(formatMessage(message, null), t);
        }
    }

    public void errorWithEvent(String eventId, String message, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error(formatMessage(message, eventId), t);
        }
    }

    public void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatMessage(message, null), args);
        }
    }

    private String formatMessage(String message, String eventId) {
        String reqId = CorrelationContext.getRequestId();
        if (eventId != null && !eventId.isBlank()) {
            return String.format("[%s] [%s] [%s] %s", serviceName, reqId, eventId, message);
        }
        return String.format("[%s] [%s] %s", serviceName, reqId, message);
    }
}
