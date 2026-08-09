package com.spaceweather.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DashboardSummaryDTO(
    String systemStatus,
    Instant timestamp,
    SpaceWeatherEventDTO latestEvent,
    RiskAssessmentDTO highestRiskAssessment,
    List<RiskAssessmentDTO> activeRiskAssessments,
    List<SatelliteDTO> satellites,
    List<RecommendationDTO> pendingRecommendations,
    List<AlertDTO> activeAlerts,
    SimulationConfigDTO simulationConfig,
    Map<String, Object> environmentMetrics,
    Map<String, Object> systemHealth
) {}
