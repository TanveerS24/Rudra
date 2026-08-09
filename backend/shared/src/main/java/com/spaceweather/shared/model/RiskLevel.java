package com.spaceweather.shared.model;

public enum RiskLevel {
    LOW(0, 25),
    MODERATE(26, 50),
    HIGH(51, 75),
    CRITICAL(76, 100);

    private final int minScore;
    private final int maxScore;

    RiskLevel(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public int getMinScore() { return minScore; }
    public int getMaxScore() { return maxScore; }

    public static RiskLevel fromScore(double score) {
        if (score <= 25.0) return LOW;
        if (score <= 50.0) return MODERATE;
        if (score <= 75.0) return HIGH;
        return CRITICAL;
    }
}
