package com.spaceweather.shared.error;

public class ServiceUnavailableException extends AppException {
    public ServiceUnavailableException(String message) {
        super(503, "SERVICE_UNAVAILABLE", message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(503, "SERVICE_UNAVAILABLE", message, cause);
    }
}
