package com.spaceweather.shared.model;

public enum SensitivityLevel {
    LOW(1.0),
    MEDIUM(1.3),
    HIGH(1.7),
    CRITICAL(2.2);

    private final double multiplier;

    SensitivityLevel(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
