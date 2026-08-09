package com.spaceweather.backend.risk;

import com.spaceweather.shared.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicRiskEngineTest {
    private DeterministicRiskEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DeterministicRiskEngine();
    }

    @Test
    @DisplayName("Should score higher for extreme X-class flare with G5 geomagnetic storm than moderate M-flare")
    void testExtremeVsModerateEvent() {
        SpaceWeatherEvent extremeEvent = new SpaceWeatherEvent(
                "EVT-EXT-1",
                Instant.now(),
                EventType.SOLAR_FLARE,
                "X3.5",
                60,
                850.0,
                9,
                "CRITICAL",
                new Location(10.0, 20.0),
                List.of("Global"),
                new Location(25.0, -80.0),
                "Extreme X-class event",
                0.95
        );

        SpaceWeatherEvent moderateEvent = new SpaceWeatherEvent(
                "EVT-MOD-1",
                Instant.now(),
                EventType.SOLAR_FLARE,
                "M2.1",
                30,
                450.0,
                3,
                "NORMAL",
                new Location(10.0, 20.0),
                List.of("Subauroral"),
                new Location(25.0, -80.0),
                "Moderate M-class event",
                0.85
        );

        Satellite iss = new Satellite(
                "SAT-ISS", "ISS Alpha", "Human Spaceflight", OrbitType.LEO,
                420.0, 51.6, 25.0, -80.0, HealthStatus.NOMINAL,
                SensitivityLevel.HIGH, SensitivityLevel.HIGH, SensitivityLevel.HIGH,
                OperationalStatus.ACTIVE, Instant.now(), Instant.now()
        );

        double extremeScore = engine.calculateScore(extremeEvent, iss);
        double moderateScore = engine.calculateScore(moderateEvent, iss);

        assertTrue(extremeScore > 75.0, "Extreme score should exceed 75 (CRITICAL). Actual: " + extremeScore);
        assertTrue(moderateScore < 50.0, "Moderate score should be under 50. Actual: " + moderateScore);
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromScore(extremeScore));
        assertEquals(RiskLevel.MODERATE, RiskLevel.fromScore(moderateScore));
    }

    @Test
    @DisplayName("Different satellites should have different vulnerability scores under identical event")
    void testDifferentialVulnerability() {
        SpaceWeatherEvent event = new SpaceWeatherEvent(
                "EVT-GEO-1",
                Instant.now(),
                EventType.GEOMAGNETIC_STORM,
                "G4_SEVERE",
                90,
                680.0,
                8,
                "HIGH",
                new Location(0.0, 0.0),
                List.of("Polar"),
                new Location(65.0, 20.0),
                "G4 Severe storm",
                0.92
        );

        // High sensitivity satellite in HEO
        Satellite chandra = new Satellite(
                "SAT-CHANDRA", "Chandra Observatory", "Astrophysics", OrbitType.HEO,
                64000.0, 28.5, 0.0, 0.0, HealthStatus.NOMINAL,
                SensitivityLevel.CRITICAL, SensitivityLevel.HIGH, SensitivityLevel.MEDIUM,
                OperationalStatus.ACTIVE, Instant.now(), Instant.now()
        );

        // Low sensitivity weather satellite in GEO
        Satellite goes = new Satellite(
                "SAT-GOES", "GOES-18", "Weather", OrbitType.GEO,
                35786.0, 0.0, 0.0, -137.0, HealthStatus.NOMINAL,
                SensitivityLevel.LOW, SensitivityLevel.LOW, SensitivityLevel.LOW,
                OperationalStatus.ACTIVE, Instant.now(), Instant.now()
        );

        double chandraScore = engine.calculateScore(event, chandra);
        double goesScore = engine.calculateScore(event, goes);

        assertTrue(chandraScore > goesScore, "Chandra score (" + chandraScore + ") should be higher than GOES score (" + goesScore + ")");
    }
}
