package com.spaceweather.backend.decision;

import com.spaceweather.shared.model.Recommendation;
import com.spaceweather.shared.model.RiskAssessment;
import com.spaceweather.shared.model.RiskLevel;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.model.SpaceWeatherEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DecisionEngine {

    public List<Recommendation> generateRecommendations(SpaceWeatherEvent event, List<Satellite> satellites, List<RiskAssessment> assessments) {
        List<Recommendation> recommendations = new ArrayList<>();

        for (RiskAssessment assessment : assessments) {
            Satellite sat = satellites.stream()
                    .filter(s -> s.getSatelliteId().equals(assessment.getSatelliteId()))
                    .findFirst()
                    .orElse(null);

            if (sat == null) continue;

            if (assessment.getRiskLevel() == RiskLevel.CRITICAL) {
                recommendations.add(createCriticalRecommendation(event, sat, assessment));
            } else if (assessment.getRiskLevel() == RiskLevel.HIGH) {
                recommendations.add(createHighRiskRecommendation(event, sat, assessment));
            } else if (assessment.getRiskLevel() == RiskLevel.MODERATE && recommendations.size() < 3) {
                recommendations.add(createModerateRiskRecommendation(event, sat, assessment));
            }
        }

        if (recommendations.isEmpty() && !satellites.isEmpty()) {
            Satellite primary = satellites.get(0);
            String recId = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            recommendations.add(new Recommendation(
                    recId,
                    event.getEventId(),
                    null,
                    primary.getSatelliteId(),
                    "Maintain Standard Orbital Station-Keeping Telemetry",
                    "Space weather conditions are within nominal baseline tolerances across constellation orbits.",
                    "Fleet operations continue without interruption.",
                    0.98,
                    "PENDING",
                    Instant.now()
            ));
        }

        return recommendations;
    }

    private Recommendation createCriticalRecommendation(SpaceWeatherEvent event, Satellite sat, RiskAssessment assessment) {
        String action;
        String reasoning;
        String impact;

        switch (sat.getOrbitType()) {
            case LEO -> {
                action = "Orient Solar Arrays to Knife-Edge Low-Drag Profile & Suspend Extravehicular Activity";
                reasoning = String.format("Thermospheric expansion from severe geomagnetic activity (Kp=%d) induces peak aerodynamic drag on %s. Critical radiation threshold exceeded.", event.getGeomagneticIndex(), sat.getName());
                impact = "Reduces orbital altitude decay rate by up to 65% and ensures payload electronics survival.";
            }
            case MEO -> {
                action = "Enable Memory Parity Scrubbing & Re-Route Secondary Navigation Ephemeris";
                reasoning = String.format("Intense particle injection into radiation belt exposes %s payload to high SEU latch-up risk.", sat.getName());
                impact = "Prevents onboard computer freezes and maintains timing synchronicity for ground networks.";
            }
            case GEO -> {
                action = "Activate Spacecraft Grounding Discharging Routine & Bias Transponder Amplifiers";
                reasoning = String.format("High dynamic pressure solar wind (%.0f km/s) causes severe dielectric charging on %s outer blanket.", event.getSolarWindSpeed(), sat.getName());
                impact = "Eliminates electrostatic discharge arcs and preserves continuous communications transponder link.";
            }
            default -> {
                action = "Transition Payload to Safe-Mode Telemetry & Secure Sensitive Detectors";
                reasoning = String.format("Extreme environmental risk (Score: %.1f) threatens mission systems on %s.", assessment.getFinalScore(), sat.getName());
                impact = "Guarantees satellite survival until space weather disturbance subsides.";
            }
        }

        String recId = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new Recommendation(
                recId,
                event.getEventId(),
                assessment.getAssessmentId(),
                sat.getSatelliteId(),
                action,
                reasoning,
                impact,
                0.94,
                "PENDING",
                Instant.now()
        );
    }

    private Recommendation createHighRiskRecommendation(SpaceWeatherEvent event, Satellite sat, RiskAssessment assessment) {
        String action = String.format("Increase Telemetry Polling Rate & Switch %s to Redundant Downlink Channel", sat.getName());
        String reasoning = String.format("Elevated ionospheric disturbance (Radiation: %s) degrades primary RF communications margin.", event.getRadiationLevel());
        String impact = "Maintains 99.8% command link reliability throughout the event duration.";

        String recId = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new Recommendation(
                recId,
                event.getEventId(),
                assessment.getAssessmentId(),
                sat.getSatelliteId(),
                action,
                reasoning,
                impact,
                0.89,
                "PENDING",
                Instant.now()
        );
    }

    private Recommendation createModerateRiskRecommendation(SpaceWeatherEvent event, Satellite sat, RiskAssessment assessment) {
        String action = String.format("Pre-stage Contingency Attitude Recovery Sequence for %s", sat.getName());
        String reasoning = "Moderate space weather activity detected; early readiness advised in case of unexpected flare escalation.";
        String impact = "Reduces operational response latency from 15 minutes to under 30 seconds.";

        String recId = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new Recommendation(
                recId,
                event.getEventId(),
                assessment.getAssessmentId(),
                sat.getSatelliteId(),
                action,
                reasoning,
                impact,
                0.82,
                "PENDING",
                Instant.now()
        );
    }
}
