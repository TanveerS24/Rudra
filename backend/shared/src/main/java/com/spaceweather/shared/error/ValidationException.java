package com.spaceweather.shared.error;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(400, "VALIDATION_FAILED", message);
    }

    public ValidationException(String message, Throwable cause) {
        super(400, "VALIDATION_FAILED", message, cause);
    }
}
