package com.spaceweather.simulation.api;

import com.spaceweather.simulation.application.SimulationEngine;
import com.spaceweather.simulation.feedback.FeedbackService;
import com.spaceweather.simulation.memory.MemoryStore;
import com.spaceweather.shared.dto.FeedbackScoreDTO;
import com.spaceweather.shared.dto.HealthStatusDTO;
import com.spaceweather.shared.dto.SimulationConfigDTO;
import com.spaceweather.shared.dto.SpaceWeatherEventDTO;
import com.spaceweather.shared.model.EventType;
import com.spaceweather.shared.model.FeedbackScore;
import com.spaceweather.shared.model.MemoryChunk;
import com.spaceweather.shared.model.SimulationConfig;
import com.spaceweather.shared.model.SpaceWeatherEvent;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SimulatorController implements HttpHandler {
    private final SimulationEngine engine;
    private final MemoryStore memoryStore;
    private final FeedbackService feedbackService;

    public SimulatorController(SimulationEngine engine, MemoryStore memoryStore, FeedbackService feedbackService) {
        this.engine = engine;
        this.memoryStore = memoryStore;
        this.feedbackService = feedbackService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorrelationContext.setRequestId(exchange.getRequestHeaders().getFirst("X-Request-ID"));
        HttpUtils.setCorsHeaders(exchange);

        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if (path.endsWith("/config") && "GET".equals(method)) {
                SimulationConfig cfg = engine.getCurrentConfig();
                HttpUtils.sendJsonResponse(exchange, 200, toConfigDTO(cfg));
            } else if (path.endsWith("/config") && "PUT".equals(method)) {
                Map<?, ?> body = HttpUtils.readJsonBody(exchange, Map.class);
                SimulationConfig current = engine.getCurrentConfig();
                int interval = body.get("intervalSeconds") instanceof Number n ? n.intValue() : current.getIntervalSeconds();
                int workers = body.get("workerCount") instanceof Number n ? n.intValue() : current.getWorkerCount();
                String mode = (String) body.get("mode");
                if (mode == null || mode.isBlank()) mode = current.getMode();
                String intensity = (String) body.get("defaultIntensity");
                if (intensity == null || intensity.isBlank()) intensity = current.getDefaultIntensity();

                engine.updateConfig(interval, workers, mode, intensity);
                HttpUtils.sendJsonResponse(exchange, 200, toConfigDTO(engine.getCurrentConfig()));
            } else if (path.endsWith("/start") && "POST".equals(method)) {
                engine.startScheduler();
                HttpUtils.sendJsonResponse(exchange, 200, Map.of("status", "STARTED", "active", true));
            } else if (path.endsWith("/pause") && "POST".equals(method)) {
                engine.pauseScheduler();
                HttpUtils.sendJsonResponse(exchange, 200, Map.of("status", "PAUSED", "active", false));
            } else if (path.endsWith("/reset") && "POST".equals(method)) {
                engine.resetSimulation();
                HttpUtils.sendJsonResponse(exchange, 200, Map.of("status", "RESET", "active", false));
            } else if (path.endsWith("/generate") && "POST".equals(method)) {
                Map<?, ?> body = exchange.getRequestBody().available() > 0 ? HttpUtils.readJsonBody(exchange, Map.class) : Map.of();
                String intensity = (String) body.get("intensity");
                String typeStr = (String) body.get("eventType");
                EventType type = typeStr != null ? EventType.valueOf(typeStr.toUpperCase()) : null;

                CompletableFuture<SpaceWeatherEvent> future = engine.triggerScenarioGeneration(intensity, type);
                SpaceWeatherEvent event = future.get(10, TimeUnit.SECONDS);
                HttpUtils.sendJsonResponse(exchange, 201, toEventDTO(event));
            } else if (path.endsWith("/status") && "GET".equals(method)) {
                Map<String, Object> status = Map.of(
                        "active", engine.getCurrentConfig().isActive(),
                        "intervalSeconds", engine.getCurrentConfig().getIntervalSeconds(),
                        "workerCount", engine.getQueue().getWorkerCount(),
                        "queueSize", engine.getQueue().getQueueSize(),
                        "processedCount", engine.getQueue().getProcessedCount(),
                        "failedCount", engine.getQueue().getFailedCount(),
                        "totalMemories", memoryStore.count(),
                        "lastGeneratedEvent", engine.getLastGeneratedEvent() != null ? toEventDTO(engine.getLastGeneratedEvent()) : "NONE"
                );
                HttpUtils.sendJsonResponse(exchange, 200, status);
            } else if (path.endsWith("/memories") && "GET".equals(method)) {
                List<MemoryChunk> list = memoryStore.retrieveHighQuality(20);
                HttpUtils.sendJsonResponse(exchange, 200, list);
            } else if (path.endsWith("/feedback") && "POST".equals(method)) {
                Map<?, ?> body = HttpUtils.readJsonBody(exchange, Map.class);
                String targetId = (String) body.get("targetId");
                String targetType = (String) body.get("targetType");
                double accuracy = body.get("accuracyScore") instanceof Number n ? n.doubleValue() : 0.8;
                double usefulness = body.get("usefulnessScore") instanceof Number n ? n.doubleValue() : 0.8;
                String comments = (String) body.get("comments");

                FeedbackScore score = feedbackService.submitFeedback(targetId, targetType, accuracy, usefulness, comments);
                HttpUtils.sendJsonResponse(exchange, 201, feedbackService.toDTO(score));
            } else if (path.endsWith("/health") || path.endsWith("/ready")) {
                HealthStatusDTO health = new HealthStatusDTO(
                        "simulation-service",
                        "UP",
                        Instant.now(),
                        Map.of(
                                "queueSize", engine.getQueue().getQueueSize(),
                                "activeWorkers", engine.getQueue().getWorkerCount(),
                                "memoryChunks", memoryStore.count()
                        )
                );
                HttpUtils.sendJsonResponse(exchange, 200, health);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        } catch (Throwable t) {
            HttpUtils.sendErrorResponse(exchange, t, "SIMULATION-SERVICE");
        } finally {
            CorrelationContext.clear();
        }
    }

    private SimulationConfigDTO toConfigDTO(SimulationConfig c) {
        return new SimulationConfigDTO(
                c.getIntervalSeconds(),
                c.getWorkerCount(),
                c.getMode(),
                c.getDefaultIntensity(),
                c.isActive(),
                c.getUpdatedAt()
        );
    }

    private SpaceWeatherEventDTO toEventDTO(SpaceWeatherEvent event) {
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
}
