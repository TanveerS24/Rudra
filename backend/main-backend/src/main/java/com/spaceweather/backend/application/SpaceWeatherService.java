package com.spaceweather.backend.application;

import com.spaceweather.backend.decision.DecisionEngine;
import com.spaceweather.backend.persistence.*;
import com.spaceweather.backend.risk.HybridRiskAggregator;
import com.spaceweather.backend.websocket.SpaceWeatherWebSocketServer;
import com.spaceweather.shared.dto.*;
import com.spaceweather.shared.model.*;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.StructuredLogger;

import java.time.Instant;
import java.util.*;

public class SpaceWeatherService {
    private static final StructuredLogger log = StructuredLogger.of(SpaceWeatherService.class, "MAIN-BACKEND");

    private final EventRepository eventRepository;
    private final SatelliteRepository satelliteRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RecommendationRepository recommendationRepository;
    private final AlertRepository alertRepository;
    private final HybridRiskAggregator riskAggregator;
    private final DecisionEngine decisionEngine;
    private final SpaceWeatherWebSocketServer wsServer;

    public SpaceWeatherService(
            EventRepository eventRepository,
            SatelliteRepository satelliteRepository,
            RiskAssessmentRepository riskAssessmentRepository,
            RecommendationRepository recommendationRepository,
            AlertRepository alertRepository,
            HybridRiskAggregator riskAggregator,
            DecisionEngine decisionEngine,
            SpaceWeatherWebSocketServer wsServer) {
        this.eventRepository = eventRepository;
        this.satelliteRepository = satelliteRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.recommendationRepository = recommendationRepository;
        this.alertRepository = alertRepository;
        this.riskAggregator = riskAggregator;
        this.decisionEngine = decisionEngine;
        this.wsServer = wsServer;
    }

    public synchronized SpaceWeatherEvent processEvent(SpaceWeatherEvent event) {
        Objects.requireNonNull(event, "event cannot be null");
        String eventId = event.getEventId();
        String reqId = CorrelationContext.getRequestId();

        // 1. Idempotency check (Section 40)
        if (eventRepository.existsById(eventId)) {
            log.infoWithEvent(eventId, "Duplicate event received. Returning existing event record.");
            return eventRepository.findById(eventId).orElse(event);
        }

        log.infoWithEvent(eventId, "Processing space weather event: {} (Intensity: {}, Kp: {})",
                event.getEventType(), event.getIntensity(), event.getGeomagneticIndex());

        // 2. Persist event
        eventRepository.save(event);

        // 3. Assess risk across satellite constellation
        List<Satellite> satellites = satelliteRepository.findAll();
        List<RiskAssessment> assessments = new ArrayList<>();

        for (Satellite sat : satellites) {
            RiskAssessment assessment = riskAggregator.assessRisk(event, sat);
            riskAssessmentRepository.save(assessment);
            assessments.add(assessment);

            // Update satellite health if risk is critical
            if (assessment.getRiskLevel() == RiskLevel.CRITICAL) {
                satelliteRepository.updateHealthAndStatus(sat.getSatelliteId(), HealthStatus.DEGRADED, OperationalStatus.SAFE_MODE);
            }
        }

        // 4. Generate operational decision recommendations
        List<Recommendation> recommendations = decisionEngine.generateRecommendations(event, satellites, assessments);
        for (Recommendation rec : recommendations) {
            recommendationRepository.save(rec);
        }

        // 5. Generate system alerts if necessary
        if (event.getGeomagneticIndex() >= 6 || event.getIntensity().toUpperCase().startsWith("X")) {
            String alertSeverity = event.getIntensity().toUpperCase().startsWith("X") ? "CRITICAL" : "WARNING";
            Alert alert = new Alert(
                    "ALT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    eventId,
                    alertSeverity,
                    String.format("%s Space Weather Alert: %s (%s)", alertSeverity, event.getEventType(), event.getIntensity()),
                    String.format("Impact detected at coordinates (%.2f, %.2f). Recommended action: %s",
                            event.getMaximumImpactLocation().getLatitude(),
                            event.getMaximumImpactLocation().getLongitude(),
                            recommendations.isEmpty() ? "Monitor telemetry" : recommendations.get(0).getAction()),
                    false,
                    Instant.now()
            );
            alertRepository.save(alert);
            if (wsServer != null) {
                wsServer.broadcastMessage("ALERT_CREATED", toAlertDTO(alert), reqId);
            }
        }

        // 6. Broadcast real-time updates via WebSocket
        if (wsServer != null) {
            wsServer.broadcastMessage("SPACE_WEATHER_EVENT", toEventDTO(event), reqId);
            wsServer.broadcastMessage("RISK_UPDATED", assessments.stream().map(a -> toRiskAssessmentDTO(a, satellites)).toList(), reqId);
            wsServer.broadcastMessage("RECOMMENDATION_CREATED", recommendations.stream().map(this::toRecommendationDTO).toList(), reqId);
            wsServer.broadcastMessage("SATELLITE_STATUS_CHANGED", satellites.stream().map(this::toSatelliteDTO).toList(), reqId);
        }

        return event;
    }

    public Optional<SpaceWeatherEvent> getLatestEvent() {
        return eventRepository.findLatest();
    }

    public Optional<SpaceWeatherEvent> getEventById(String eventId) {
        return eventRepository.findById(eventId);
    }

    public List<SpaceWeatherEvent> getRecentEvents(int limit) {
        return eventRepository.findRecent(limit);
    }

    public List<RiskAssessment> getRiskAssessmentsForEvent(String eventId) {
        return riskAssessmentRepository.findByEventId(eventId);
    }

    public List<Recommendation> getRecommendationsForEvent(String eventId) {
        return recommendationRepository.findByEventId(eventId);
    }

    public SpaceWeatherEventDTO toEventDTO(SpaceWeatherEvent event) {
        if (event == null) return null;
        return new SpaceWeatherEventDTO(
                event.getEventId(),
                event.getTimestamp(),
                event.getEventType().name(),
                event.getIntensity(),
                event.getDurationMinutes(),
                event.getSolarWindSpeed(),
                event.getGeomagneticIndex(),
                event.getRadiationLevel(),
                new SpaceWeatherEventDTO.LocationDTO(event.getOrigin().getLatitude(), event.getOrigin().getLongitude()),
                event.getAffectedRegions(),
                new SpaceWeatherEventDTO.LocationDTO(event.getMaximumImpactLocation().getLatitude(), event.getMaximumImpactLocation().getLongitude()),
                event.getImpactDescription(),
                event.getConfidence()
        );
    }

    public RiskAssessmentDTO toRiskAssessmentDTO(RiskAssessment ra, List<Satellite> satellites) {
        String satName = satellites.stream()
                .filter(s -> s.getSatelliteId().equals(ra.getSatelliteId()))
                .map(Satellite::getName)
                .findFirst()
                .orElse(ra.getSatelliteId());

        return new RiskAssessmentDTO(
                ra.getAssessmentId(),
                ra.getEventId(),
                ra.getSatelliteId(),
                satName,
                ra.getDeterministicScore(),
                ra.getFinalScore(),
                ra.getRiskLevel().name(),
                ra.getPrimaryFactors(),
                ra.getPotentialEffects(),
                ra.getCreatedAt()
        );
    }

    public SatelliteDTO toSatelliteDTO(Satellite sat) {
        if (sat == null) return null;
        return new SatelliteDTO(
                sat.getSatelliteId(),
                sat.getName(),
                sat.getMissionType(),
                sat.getOrbitType().name(),
                sat.getAltitudeKm(),
                sat.getInclinationDeg(),
                sat.getLatitude(),
                sat.getLongitude(),
                sat.getHealthStatus().name(),
                sat.getRadiationSensitivity().name(),
                sat.getCommunicationSensitivity().name(),
                sat.getNavigationSensitivity().name(),
                sat.getOperationalStatus().name(),
                sat.getCreatedAt(),
                sat.getUpdatedAt()
        );
    }

    public RecommendationDTO toRecommendationDTO(Recommendation r) {
        if (r == null) return null;
        return new RecommendationDTO(
                r.getRecommendationId(),
                r.getEventId(),
                r.getAssessmentId(),
                r.getSatelliteId(),
                r.getAction(),
                r.getReasoning(),
                r.getExpectedImpact(),
                r.getConfidence(),
                r.getStatus(),
                r.getCreatedAt()
        );
    }

    public AlertDTO toAlertDTO(Alert a) {
        if (a == null) return null;
        return new AlertDTO(
                a.getAlertId(),
                a.getEventId(),
                a.getSeverity(),
                a.getTitle(),
                a.getMessage(),
                a.isAcknowledged(),
                a.getCreatedAt()
        );
    }
}
