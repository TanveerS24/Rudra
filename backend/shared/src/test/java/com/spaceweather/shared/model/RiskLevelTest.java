package com.spaceweather.shared.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskLevelTest {

    @ParameterizedTest
    @DisplayName("Should accurately categorize risk scores into discrete levels")
    @CsvSource({
            "0.0, LOW",
            "15.5, LOW",
            "25.0, LOW",
            "25.1, MODERATE",
            "40.0, MODERATE",
            "50.0, MODERATE",
            "50.1, HIGH",
            "70.0, HIGH",
            "75.0, HIGH",
            "75.1, CRITICAL",
            "88.5, CRITICAL",
            "100.0, CRITICAL"
    })
    void testRiskLevelFromScore(double score, RiskLevel expectedLevel) {
        assertEquals(expectedLevel, RiskLevel.fromScore(score));
    }
}
