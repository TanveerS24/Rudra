package com.spaceweather.backend.decision;

import com.spaceweather.shared.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecisionEngineTest {

    @Test
    @DisplayName("Should generate critical operational directives for satellites under critical risk")
    void testCriticalRecommendationGeneration() {
        DecisionEngine engine = new DecisionEngine();

        SpaceWeatherEvent event = new SpaceWeatherEvent(
                "EVT-001", Instant.now(), EventType.CORONAL_MASS_EJECTION, "X2.0",
                60, 800.0, 8, "CRITICAL", new Location(0, 0), List.of("Global"),
                new Location(45, -70), "Severe CME", 0.95
        );

        Satellite sat = new Satellite(
                "SAT-ISS", "ISS Alpha", "Human Spaceflight", OrbitType.LEO,
                420.0, 51.6, 25.0, -80.0, HealthStatus.NOMINAL,
                SensitivityLevel.HIGH, SensitivityLevel.HIGH, SensitivityLevel.HIGH,
                OperationalStatus.ACTIVE, Instant.now(), Instant.now()
        );

        RiskAssessment assessment = new RiskAssessment(
                "RISK-001", "EVT-001", "SAT-ISS", 85.0, 85.0,
                RiskLevel.CRITICAL, List.of("High proton flux"), List.of("Severe orbital drag"), Instant.now()
        );

        List<Recommendation> recs = engine.generateRecommendations(event, List.of(sat), List.of(assessment));

        assertFalse(recs.isEmpty(), "Recommendations should not be empty");
        Recommendation rec = recs.get(0);
        assertTrue(rec.getAction().toLowerCase().contains("drag") || rec.getAction().toLowerCase().contains("solar arrays"),
                "Critical recommendation should mention solar array orientation or drag reduction");
        assertEquals("PENDING", rec.getStatus());
        assertTrue(rec.getConfidence() >= 0.85);
    }
}
