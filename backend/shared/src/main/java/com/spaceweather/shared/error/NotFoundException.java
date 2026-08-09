package com.spaceweather.shared.error;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(404, "NOT_FOUND", message);
    }
}
