package com.spaceweather.simulation.llm;

import com.spaceweather.simulation.config.SimulationServiceConfig;
import com.spaceweather.shared.model.EventType;
import com.spaceweather.shared.model.Location;
import com.spaceweather.shared.model.MemoryChunk;
import com.spaceweather.shared.model.SpaceWeatherEvent;
import com.spaceweather.shared.util.JsonUtils;
import com.spaceweather.shared.util.StructuredLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class OllamaLLMClient implements LLMClient {
    private static final StructuredLogger log = StructuredLogger.of(OllamaLLMClient.class, "SIMULATION-SERVICE");
    private final SimulationServiceConfig config;
    private final DeterministicScenarioGenerator fallbackGenerator;
    private final HttpClient httpClient;

    public OllamaLLMClient(SimulationServiceConfig config) {
        this.config = config;
        this.fallbackGenerator = new DeterministicScenarioGenerator();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public SpaceWeatherEvent generateScenario(String intensityPreference, EventType typePreference, List<MemoryChunk> contextMemories) {
        if (!config.isFallbackSimulation()) {
            try {
                SpaceWeatherEvent event = generateWithOllama(intensityPreference, typePreference, contextMemories);
                if (event != null) return event;
            } catch (Exception e) {
                log.warn("Ollama scenario generation failed: {}. Utilizing deterministic scenario generator.", e.getMessage());
            }
        }
        return fallbackGenerator.generate(intensityPreference, typePreference);
    }

    private SpaceWeatherEvent generateWithOllama(String intensity, EventType type, List<MemoryChunk> memories) throws Exception {
        StringBuilder memoryContext = new StringBuilder();
        if (memories != null && !memories.isEmpty()) {
            memoryContext.append("Relevant Historical Space Weather Memories:\n");
            for (MemoryChunk c : memories) {
                memoryContext.append("- [").append(c.getChunkType()).append("] (Score: ").append(String.format("%.2f", c.getFeedbackScore())).append("): ").append(c.getContent()).append("\n");
            }
        }

        String prompt = String.format("""
                You are an astrophysicist simulating space weather events for a satellite operations system.
                Generate ONE realistic, scientifically plausible space weather scenario.
                
                %s
                
                Parameters:
                - Preferred Type: %s
                - Target Intensity Profile: %s
                
                Respond ONLY with raw JSON in this exact structure:
                {
                  "eventType": "SOLAR_FLARE",
                  "intensity": "X2.4",
                  "durationMinutes": 45,
                  "solarWindSpeed": 620.0,
                  "geomagneticIndex": 7,
                  "radiationLevel": "HIGH",
                  "origin": {"latitude": 14.2, "longitude": 45.1},
                  "maximumImpactLocation": {"latitude": 28.5, "longitude": -80.6},
                  "affectedRegions": ["North America Subauroral", "Arctic Polar Cap"],
                  "impactDescription": "Strong flare resulting in HF radio blackouts on the sunlit side.",
                  "confidence": 0.92
                }
                """,
                memoryContext,
                type != null ? type.name() : "ANY",
                intensity != null ? intensity : "MODERATE"
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
                .timeout(Duration.ofSeconds(8))
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(reqBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Map<?, ?> json = JsonUtils.fromJson(response.body(), Map.class);
            String responseText = (String) json.get("response");
            if (responseText != null && !responseText.isBlank()) {
                return parseAndValidate(responseText);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private SpaceWeatherEvent parseAndValidate(String jsonText) {
        try {
            Map<?, ?> map = JsonUtils.fromJson(jsonText, Map.class);
            String typeStr = (String) map.get("eventType");
            EventType eventType = typeStr != null ? EventType.valueOf(typeStr.toUpperCase()) : EventType.SOLAR_FLARE;
            String intensity = (String) map.get("intensity");
            if (intensity == null || intensity.isBlank()) intensity = "M2.0";

            int duration = map.get("durationMinutes") instanceof Number n ? n.intValue() : 30;
            double solarWind = map.get("solarWindSpeed") instanceof Number n ? n.doubleValue() : 450.0;
            int kp = map.get("geomagneticIndex") instanceof Number n ? n.intValue() : 3;
            String rad = (String) map.get("radiationLevel");
            if (rad == null) rad = "NORMAL";

            Map<?, ?> originMap = (Map<?, ?>) map.get("origin");
            double oLat = originMap != null && originMap.get("latitude") instanceof Number n ? n.doubleValue() : 0.0;
            double oLon = originMap != null && originMap.get("longitude") instanceof Number n ? n.doubleValue() : 0.0;

            Map<?, ?> impactMap = (Map<?, ?>) map.get("maximumImpactLocation");
            double iLat = impactMap != null && impactMap.get("latitude") instanceof Number n ? n.doubleValue() : 0.0;
            double iLon = impactMap != null && impactMap.get("longitude") instanceof Number n ? n.doubleValue() : 0.0;

            List<String> regions = (List<String>) map.get("affectedRegions");
            if (regions == null) regions = List.of("Global Ionosphere");

            String desc = (String) map.get("impactDescription");
            double conf = map.get("confidence") instanceof Number n ? n.doubleValue() : 0.85;

            String eventId = "EVT-LLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return new SpaceWeatherEvent(
                    eventId, Instant.now(), eventType, intensity, duration,
                    solarWind, kp, rad, new Location(oLat, oLon), regions,
                    new Location(iLat, iLon), desc, conf
            );
        } catch (Exception e) {
            log.warn("Failed to parse LLM scenario JSON: {}. Error: {}", jsonText, e.getMessage());
            return null;
        }
    }
}
