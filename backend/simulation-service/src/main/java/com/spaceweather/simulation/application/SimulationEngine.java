package com.spaceweather.simulation.application;

import com.spaceweather.simulation.chunking.SemanticChunker;
import com.spaceweather.simulation.config.SimulationServiceConfig;
import com.spaceweather.simulation.llm.LLMClient;
import com.spaceweather.simulation.memory.MemoryStore;
import com.spaceweather.simulation.queue.SimulationQueue;
import com.spaceweather.simulation.queue.SimulationTask;
import com.spaceweather.shared.dto.SpaceWeatherEventDTO;
import com.spaceweather.shared.model.*;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.JsonUtils;
import com.spaceweather.shared.util.StructuredLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class SimulationEngine implements AutoCloseable {
    private static final StructuredLogger log = StructuredLogger.of(SimulationEngine.class, "SIMULATION-SERVICE");

    private final SimulationServiceConfig config;
    private final LLMClient llmClient;
    private final MemoryStore memoryStore;
    private final SemanticChunker semanticChunker;
    private final SimulationQueue queue;
    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;

    private volatile SimulationConfig currentConfig;
    private ScheduledFuture<?> scheduledTask;
    private volatile SpaceWeatherEvent lastGeneratedEvent;

    public SimulationEngine(
            SimulationServiceConfig config,
            LLMClient llmClient,
            MemoryStore memoryStore,
            SemanticChunker semanticChunker) {
        this.config = config;
        this.llmClient = llmClient;
        this.memoryStore = memoryStore;
        this.semanticChunker = semanticChunker;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        this.currentConfig = new SimulationConfig(
                config.getIntervalSeconds(),
                config.getWorkerCount(),
                "HYBRID_LLM",
                "MODERATE",
                true,
                Instant.now()
        );

        this.queue = new SimulationQueue(currentConfig.getWorkerCount(), this::executeSimulationTask);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        if (currentConfig.isActive()) {
            startScheduler();
        }
    }

    public synchronized void startScheduler() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }
        currentConfig.setActive(true);
        currentConfig.setUpdatedAt(Instant.now());

        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (currentConfig.isActive()) {
                    triggerScenarioGeneration(currentConfig.getDefaultIntensity(), null);
                }
            } catch (Exception e) {
                log.error("Error in scheduled simulation tick", e);
            }
        }, 2, currentConfig.getIntervalSeconds(), TimeUnit.SECONDS);

        log.info("Simulation scheduler started with interval: {}s", currentConfig.getIntervalSeconds());
    }

    public synchronized void pauseScheduler() {
        currentConfig.setActive(false);
        currentConfig.setUpdatedAt(Instant.now());
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        log.info("Simulation scheduler paused.");
    }

    public synchronized void resetSimulation() {
        pauseScheduler();
        lastGeneratedEvent = null;
        log.info("Simulation engine reset.");
    }

    public CompletableFuture<SpaceWeatherEvent> triggerScenarioGeneration(String intensity, EventType eventType) {
        SimulationTask task = new SimulationTask(intensity, eventType);
        queue.enqueue(task);
        return task.getFuture();
    }

    private void executeSimulationTask(SimulationTask task) {
        String reqId = CorrelationContext.getRequestId();
        try {
            log.info("Starting simulation pipeline for task {} (Intensity: {})", task.getTaskId(), task.getIntensityPreference());

            // 1. Memory Retrieval (RAG context construction)
            String query = (task.getIntensityPreference() != null ? task.getIntensityPreference() : "Space weather solar flare geomagnetic activity");
            List<MemoryChunk> relevantMemories = memoryStore.retrieveSimilar(query, 3);
            log.debug("Retrieved {} relevant semantic memories for RAG context", relevantMemories.size());

            // 2. Generate Scenario via LLM or physics generator
            SpaceWeatherEvent event = llmClient.generateScenario(
                    task.getIntensityPreference(),
                    task.getTypePreference(),
                    relevantMemories
            );

            // 3. Schema Validation & Normalization
            if (event == null) {
                throw new IllegalStateException("Generated scenario was null");
            }
            this.lastGeneratedEvent = event;

            // 4. Semantic Chunking across all 5 event categories
            MemoryChunk chkEvent = semanticChunker.chunkEvent(event);
            MemoryChunk chkEnv = semanticChunker.chunkEnvironment(event);
            MemoryChunk chkGeo = semanticChunker.chunkGeographicImpact(event);
            MemoryChunk chkSat = semanticChunker.chunkSatelliteImpact(event);
            MemoryChunk chkOps = semanticChunker.chunkOperationalImpact(event);

            // 5. Store in Persistent Semantic Memory
            memoryStore.store(chkEvent);
            memoryStore.store(chkEnv);
            memoryStore.store(chkGeo);
            memoryStore.store(chkSat);
            memoryStore.store(chkOps);

            // 6. Forward event to Main Backend
            forwardToMainBackend(event);

            task.getFuture().complete(event);
            log.infoWithEvent(event.getEventId(), "Simulation pipeline completed successfully for {}", event.getEventType());

        } catch (Exception e) {
            log.error("Simulation pipeline failed for task " + task.getTaskId(), e);
            task.getFuture().completeExceptionally(e);
        } finally {
            CorrelationContext.clear();
        }
    }

    private void forwardToMainBackend(SpaceWeatherEvent event) {
        try {
            SpaceWeatherEventDTO dto = new SpaceWeatherEventDTO(
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

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getMainBackendUrl() + "/api/v1/events"))
                    .header("Content-Type", "application/json")
                    .header("X-Request-ID", CorrelationContext.getRequestId())
                    .timeout(Duration.ofSeconds(4))
                    .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(dto)))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                log.infoWithEvent(event.getEventId(), "Forwarded simulated event to Main Backend successfully.");
            } else {
                log.warn("Main Backend returned status {} when forwarding event {}", resp.statusCode(), event.getEventId());
            }
        } catch (Exception e) {
            log.warn("Failed to forward event to Main Backend at {}: {}. Event stored in simulation memory.",
                    config.getMainBackendUrl(), e.getMessage());
        }
    }

    public SimulationConfig getCurrentConfig() { return currentConfig; }

    public synchronized void updateConfig(int intervalSeconds, int workerCount, String mode, String defaultIntensity) {
        currentConfig.setIntervalSeconds(intervalSeconds);
        currentConfig.setWorkerCount(workerCount);
        if (mode != null) currentConfig.setMode(mode);
        if (defaultIntensity != null) currentConfig.setDefaultIntensity(defaultIntensity);
        currentConfig.setUpdatedAt(Instant.now());

        if (currentConfig.isActive()) {
            startScheduler();
        }
    }

    public SimulationQueue getQueue() { return queue; }
    public SpaceWeatherEvent getLastGeneratedEvent() { return lastGeneratedEvent; }

    @Override
    public void close() {
        scheduler.shutdown();
        queue.close();
    }
}
