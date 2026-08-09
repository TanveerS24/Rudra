package com.spaceweather.backend.application;

import com.spaceweather.backend.config.AppConfig;
import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.backend.persistence.*;
import com.spaceweather.shared.dto.*;
import com.spaceweather.shared.model.RiskAssessment;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.model.SpaceWeatherEvent;

import java.time.Instant;
import java.util.*;

public class DashboardService {
    private final SpaceWeatherService spaceWeatherService;
    private final SatelliteService satelliteService;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RecommendationRepository recommendationRepository;
    private final AlertRepository alertRepository;
    private final DatabaseConnectionPool dbPool;
    private final AppConfig appConfig;

    public DashboardService(
            SpaceWeatherService spaceWeatherService,
            SatelliteService satelliteService,
            RiskAssessmentRepository riskAssessmentRepository,
            RecommendationRepository recommendationRepository,
            AlertRepository alertRepository,
            DatabaseConnectionPool dbPool,
            AppConfig appConfig) {
        this.spaceWeatherService = spaceWeatherService;
        this.satelliteService = satelliteService;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.recommendationRepository = recommendationRepository;
        this.alertRepository = alertRepository;
        this.dbPool = dbPool;
        this.appConfig = appConfig;
    }

    public DashboardSummaryDTO getDashboardSummary() {
        Optional<SpaceWeatherEvent> latestEvt = spaceWeatherService.getLatestEvent();
        List<Satellite> satellites = satelliteService.getAllSatellites();
        List<SatelliteDTO> satelliteDTOs = satellites.stream().map(satelliteService::toDTO).toList();

        List<RiskAssessmentDTO> activeAssessments = Collections.emptyList();
        RiskAssessmentDTO highestRisk = null;

        if (latestEvt.isPresent()) {
            List<RiskAssessment> assessments = riskAssessmentRepository.findByEventId(latestEvt.get().getEventId());
            activeAssessments = assessments.stream().map(a -> spaceWeatherService.toRiskAssessmentDTO(a, satellites)).toList();
            if (!activeAssessments.isEmpty()) {
                highestRisk = activeAssessments.get(0);
            }
        }

        List<RecommendationDTO> pendingRecs = recommendationRepository.findPending().stream()
                .map(spaceWeatherService::toRecommendationDTO).toList();

        List<AlertDTO> activeAlerts = alertRepository.findActive().stream()
                .map(spaceWeatherService::toAlertDTO).toList();

        Map<String, Object> envMetrics = new HashMap<>();
        if (latestEvt.isPresent()) {
            SpaceWeatherEvent e = latestEvt.get();
            envMetrics.put("solarWindSpeed", e.getSolarWindSpeed());
            envMetrics.put("geomagneticIndex", e.getGeomagneticIndex());
            envMetrics.put("radiationLevel", e.getRadiationLevel());
            envMetrics.put("intensity", e.getIntensity());
            envMetrics.put("eventType", e.getEventType().name());
            envMetrics.put("durationMinutes", e.getDurationMinutes());
        } else {
            envMetrics.put("solarWindSpeed", 420.0);
            envMetrics.put("geomagneticIndex", 3);
            envMetrics.put("radiationLevel", "NORMAL");
            envMetrics.put("intensity", "M1.2");
            envMetrics.put("eventType", "SOLAR_FLARE");
            envMetrics.put("durationMinutes", 30);
        }

        Map<String, Object> sysHealth = Map.of(
                "mainBackend", "ONLINE",
                "database", dbPool.isConnected() ? "CONNECTED" : "FALLBACK_MEMORY",
                "websocket", "ACTIVE",
                "ollamaLLM", appConfig.isDeterministicFallback() ? "FALLBACK_READY" : "ONLINE"
        );

        return new DashboardSummaryDTO(
                "SYSTEM ONLINE",
                Instant.now(),
                latestEvt.map(spaceWeatherService::toEventDTO).orElse(null),
                highestRisk,
                activeAssessments,
                satelliteDTOs,
                pendingRecs,
                activeAlerts,
                new SimulationConfigDTO(15, 2, "HYBRID_LLM", "MODERATE", true, Instant.now()),
                envMetrics,
                sysHealth
        );
    }
}
