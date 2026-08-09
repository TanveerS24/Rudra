package com.spaceweather.backend.risk;

import com.spaceweather.backend.config.AppConfig;
import com.spaceweather.shared.model.RiskLevel;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.model.SpaceWeatherEvent;
import com.spaceweather.shared.util.JsonUtils;
import com.spaceweather.shared.util.StructuredLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class LLMRiskExplainer {
    private static final StructuredLogger log = StructuredLogger.of(LLMRiskExplainer.class, "MAIN-BACKEND");
    private final AppConfig config;
    private final HttpClient httpClient;

    public record ExplanationResult(List<String> primaryFactors, List<String> potentialEffects) {}

    public LLMRiskExplainer(AppConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public ExplanationResult explain(SpaceWeatherEvent event, Satellite satellite, double deterministicScore, RiskLevel riskLevel) {
        if (!config.isDeterministicFallback()) {
            try {
                return explainWithLLM(event, satellite, deterministicScore, riskLevel);
            } catch (Exception e) {
                log.warn("LLM explanation failed: {}. Falling back to deterministic rule explainer.", e.getMessage());
            }
        }
        return explainDeterministic(event, satellite, deterministicScore, riskLevel);
    }

    private ExplanationResult explainWithLLM(SpaceWeatherEvent event, Satellite satellite, double score, RiskLevel level) throws Exception {
        String prompt = String.format("""
                You are a Space Operations Risk Analyst AI. Given the space weather event and satellite profile, produce a JSON object with two fields:
                1. "primaryFactors": a list of 2 to 3 concise bullet strings explaining the physical causes of risk.
                2. "potentialEffects": a list of 2 to 3 concise bullet strings explaining operational impacts on this specific satellite.
                
                Event Details:
                - Type: %s
                - Intensity: %s
                - Solar Wind: %.1f km/s
                - Geomagnetic Kp: %d
                - Radiation Level: %s
                
                Satellite Profile:
                - Name: %s
                - Orbit: %s (%.1f km)
                - Radiation Sensitivity: %s
                - Comm Sensitivity: %s
                - Nav Sensitivity: %s
                - Calculated Risk Score: %.1f (%s)
                
                Respond ONLY with raw valid JSON in this exact structure:
                {"primaryFactors": ["..."], "potentialEffects": ["..."]}
                """,
                event.getEventType(), event.getIntensity(), event.getSolarWindSpeed(), event.getGeomagneticIndex(), event.getRadiationLevel(),
                satellite.getName(), satellite.getOrbitType(), satellite.getAltitudeKm(),
                satellite.getRadiationSensitivity(), satellite.getCommunicationSensitivity(), satellite.getNavigationSensitivity(),
                score, level
        );

        Map<String, Object> reqBody = Map.of(
                "model", config.getOllamaModel(),
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getOllamaUrl() + "/api/generate"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(6))
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(reqBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Map<?, ?> json = JsonUtils.fromJson(response.body(), Map.class);
            String responseText = (String) json.get("response");
            if (responseText != null && !responseText.isBlank()) {
                Map<?, ?> parsed = JsonUtils.fromJson(responseText, Map.class);
                List<String> factors = (List<String>) parsed.get("primaryFactors");
                List<String> effects = (List<String>) parsed.get("potentialEffects");
                if (factors != null && effects != null && !factors.isEmpty()) {
                    return new ExplanationResult(factors, effects);
                }
            }
        }
        return explainDeterministic(event, satellite, score, level);
    }

    public ExplanationResult explainDeterministic(SpaceWeatherEvent event, Satellite satellite, double score, RiskLevel level) {
        List<String> factors = new ArrayList<>();
        List<String> effects = new ArrayList<>();

        if (event.getIntensity().toUpperCase().startsWith("X")) {
            factors.add("Extreme X-class solar flare producing high ionization flux across dayside ionosphere");
        } else if (event.getIntensity().toUpperCase().startsWith("M")) {
            factors.add("Moderate M-class solar flare emission inducing radio propagation disturbances");
        }

        if (event.getGeomagneticIndex() >= 6) {
            factors.add(String.format("Severe geomagnetic storm activity (Kp=%d) causing magnetospheric compression", event.getGeomagneticIndex()));
        }

        if (event.getSolarWindSpeed() >= 600.0) {
            factors.add(String.format("High-speed solar wind stream (%.0f km/s) exerting dynamic solar pressure", event.getSolarWindSpeed()));
        }

        if (factors.isEmpty()) {
            factors.add("Baseline ambient solar wind conditions within nominal operational tolerance");
        }

        // Satellite specific effects
        switch (satellite.getOrbitType()) {
            case LEO -> {
                if (event.getGeomagneticIndex() >= 6) {
                    effects.add("Enhanced thermospheric density increasing orbital aerodynamic drag");
                }
                if (satellite.getRadiationSensitivity().getMultiplier() >= 1.5) {
                    effects.add("Single Event Upset (SEU) bit-flip vulnerability in polar orbit crossing");
                }
            }
            case MEO -> {
                effects.add("High-energy electron injection in outer Van Allen radiation belt");
                if (satellite.getNavigationSensitivity().getMultiplier() >= 1.5) {
                    effects.add("Scintillation on L-band downlink signals causing carrier tracking loss");
                }
            }
            case GEO -> {
                effects.add("Surface spacecraft charging and differential electrostatic discharge risk");
                if (satellite.getCommunicationSensitivity().getMultiplier() >= 1.5) {
                    effects.add("Ku/Ka-band transponder noise floor elevation and margin degradation");
                }
            }
            case HEO -> {
                effects.add("Extended exposure to unshielded solar proton radiation outside magnetosphere");
            }
        }

        if (effects.isEmpty()) {
            effects.add("Minor signal telemetry variation; operations remain nominal");
        }

        return new ExplanationResult(factors, effects);
    }
}
