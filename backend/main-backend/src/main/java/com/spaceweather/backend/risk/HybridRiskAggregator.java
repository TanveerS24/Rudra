package com.spaceweather.backend.risk;

import com.spaceweather.shared.model.RiskAssessment;
import com.spaceweather.shared.model.RiskLevel;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.model.SpaceWeatherEvent;

import java.time.Instant;
import java.util.UUID;

public class HybridRiskAggregator {
    private final DeterministicRiskEngine deterministicEngine;
    private final LLMRiskExplainer explainer;

    public HybridRiskAggregator(DeterministicRiskEngine deterministicEngine, LLMRiskExplainer explainer) {
        this.deterministicEngine = deterministicEngine;
        this.explainer = explainer;
    }

    public RiskAssessment assessRisk(SpaceWeatherEvent event, Satellite satellite) {
        double detScore = deterministicEngine.calculateScore(event, satellite);
        RiskLevel riskLevel = RiskLevel.fromScore(detScore);
        LLMRiskExplainer.ExplanationResult explanation = explainer.explain(event, satellite, detScore, riskLevel);

        String assessmentId = "RISK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new RiskAssessment(
                assessmentId,
                event.getEventId(),
                satellite.getSatelliteId(),
                detScore,
                detScore, // Authoritative final score preserves deterministic safety guarantees
                riskLevel,
                explanation.primaryFactors(),
                explanation.potentialEffects(),
                Instant.now()
        );
    }
}
