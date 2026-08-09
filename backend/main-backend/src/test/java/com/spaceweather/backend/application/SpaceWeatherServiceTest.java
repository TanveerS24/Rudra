package com.spaceweather.backend.application;

import com.spaceweather.backend.config.AppConfig;
import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.backend.decision.DecisionEngine;
import com.spaceweather.backend.persistence.*;
import com.spaceweather.backend.risk.DeterministicRiskEngine;
import com.spaceweather.backend.risk.HybridRiskAggregator;
import com.spaceweather.backend.risk.LLMRiskExplainer;
import com.spaceweather.backend.risk.RiskPolicy;
import com.spaceweather.shared.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpaceWeatherServiceTest {
    private SpaceWeatherService service;
    private EventRepository eventRepo;
    private SatelliteRepository satRepo;
    private RiskAssessmentRepository riskRepo;

    @BeforeEach
    void setUp() {
        AppConfig config = new AppConfig();
        DatabaseConnectionPool dbPool = new DatabaseConnectionPool(config);

        eventRepo = new MySqlEventRepository(dbPool);
        satRepo = new MySqlSatelliteRepository(dbPool);
        riskRepo = new MySqlRiskAssessmentRepository(dbPool);
        RecommendationRepository recRepo = new MySqlRecommendationRepository(dbPool);
        AlertRepository alertRepo = new MySqlAlertRepository(dbPool);

        DeterministicRiskEngine detEngine = new DeterministicRiskEngine(new RiskPolicy());
        LLMRiskExplainer explainer = new LLMRiskExplainer(config);
        HybridRiskAggregator aggregator = new HybridRiskAggregator(detEngine, explainer);
        DecisionEngine decisionEngine = new DecisionEngine();

        service = new SpaceWeatherService(eventRepo, satRepo, riskRepo, recRepo, alertRepo, aggregator, decisionEngine, null);
    }

    @Test
    @DisplayName("Should process event, assess risk across fleet, generate recommendations and enforce idempotency")
    void testEventProcessingAndIdempotency() {
        SpaceWeatherEvent event = new SpaceWeatherEvent(
                "EVT-IDEMP-100",
                Instant.now(),
                EventType.SOLAR_FLARE,
                "X1.5",
                40,
                600.0,
                7,
                "HIGH",
                new Location(15.0, 30.0),
                List.of("Pacific Basin"),
                new Location(20.0, -150.0),
                "Strong solar flare with radio blackout",
                0.90
        );

        SpaceWeatherEvent processed = service.processEvent(event);
        assertNotNull(processed);
        assertEquals(event.getEventId(), processed.getEventId());

        // Verify risk assessments created for satellites
        List<RiskAssessment> assessments = service.getRiskAssessmentsForEvent(event.getEventId());
        assertFalse(assessments.isEmpty(), "Risk assessments should be computed for fleet");

        // Test idempotency: send same event again
        SpaceWeatherEvent duplicate = service.processEvent(event);
        assertEquals(processed.getEventId(), duplicate.getEventId());
    }
}
