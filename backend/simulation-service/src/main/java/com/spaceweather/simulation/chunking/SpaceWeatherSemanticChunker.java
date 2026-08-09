package com.spaceweather.simulation.chunking;

import com.spaceweather.simulation.memory.EmbeddingService;
import com.spaceweather.shared.model.ChunkType;
import com.spaceweather.shared.model.MemoryChunk;
import com.spaceweather.shared.model.SpaceWeatherEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SpaceWeatherSemanticChunker implements SemanticChunker {
    private final EmbeddingService embeddingService;

    public SpaceWeatherSemanticChunker(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @Override
    public MemoryChunk chunkEvent(SpaceWeatherEvent event) {
        String content = String.format(
                "Space weather event %s of type %s with intensity %s detected. Duration estimated at %d minutes. Source confidence: %.2f.",
                event.getEventId(), event.getEventType(), event.getIntensity(), event.getDurationMinutes(), event.getConfidence()
        );
        List<Double> emb = embeddingService.computeEmbedding(content);
        return new MemoryChunk(
                "CHK-EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                event.getEventId(),
                ChunkType.EVENT,
                content,
                1.2,
                0.85,
                emb,
                Instant.now()
        );
    }

    @Override
    public MemoryChunk chunkEnvironment(SpaceWeatherEvent event) {
        String content = String.format(
                "Environmental solar wind velocity reached %.1f km/s with planetary geomagnetic Kp index of %d and radiation flux level %s.",
                event.getSolarWindSpeed(), event.getGeomagneticIndex(), event.getRadiationLevel()
        );
        List<Double> emb = embeddingService.computeEmbedding(content);
        return new MemoryChunk(
                "CHK-ENV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                event.getEventId(),
                ChunkType.ENVIRONMENT,
                content,
                1.0,
                0.80,
                emb,
                Instant.now()
        );
    }

    @Override
    public MemoryChunk chunkGeographicImpact(SpaceWeatherEvent event) {
        String regions = String.join(", ", event.getAffectedRegions());
        String content = String.format(
                "Maximum modeled terrestrial impact location at (%.2f°, %.2f°). Affected geographic zones include: [%s]. Description: %s",
                event.getMaximumImpactLocation().getLatitude(),
                event.getMaximumImpactLocation().getLongitude(),
                regions,
                event.getImpactDescription()
        );
        List<Double> emb = embeddingService.computeEmbedding(content);
        return new MemoryChunk(
                "CHK-GEO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                event.getEventId(),
                ChunkType.GEOGRAPHIC_IMPACT,
                content,
                1.3,
                0.82,
                emb,
                Instant.now()
        );
    }

    @Override
    public MemoryChunk chunkSatelliteImpact(SpaceWeatherEvent event) {
        String content = String.format(
                "Space weather conditions (Kp: %d, Radiation: %s) increase thermospheric drag on LEO constellations and SEU bit-flip risks across polar crossings.",
                event.getGeomagneticIndex(), event.getRadiationLevel()
        );
        List<Double> emb = embeddingService.computeEmbedding(content);
        return new MemoryChunk(
                "CHK-SAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                event.getEventId(),
                ChunkType.SATELLITE_IMPACT,
                content,
                1.4,
                0.88,
                emb,
                Instant.now()
        );
    }

    @Override
    public MemoryChunk chunkOperationalImpact(SpaceWeatherEvent event) {
        String content = String.format(
                "Operational risk elevated for radio communications and satellite navigation sectors due to event %s. Pre-emptive maneuvers recommended.",
                event.getEventId()
        );
        List<Double> emb = embeddingService.computeEmbedding(content);
        return new MemoryChunk(
                "CHK-OPS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                event.getEventId(),
                ChunkType.OPERATIONAL_IMPACT,
                content,
                1.1,
                0.80,
                emb,
                Instant.now()
        );
    }

    @Override
    public MemoryChunk chunkRecommendation(String eventId, String action, String reasoning) {
        String content = String.format(
                "Operational recommendation for event %s: '%s'. Reasoning: %s.",
                eventId, action, reasoning
        );
        List<Double> emb = embeddingService.computeEmbedding(content);
        return new MemoryChunk(
                "CHK-REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                eventId,
                ChunkType.RECOMMENDATION,
                content,
                1.6,
                0.90,
                emb,
                Instant.now()
        );
    }

    @Override
    public MemoryChunk chunkFeedback(String targetId, double score, String feedbackText) {
        String content = String.format(
                "Feedback recorded for target %s with score %.2f: %s",
                targetId, score, feedbackText
        );
        List<Double> emb = embeddingService.computeEmbedding(content);
        return new MemoryChunk(
                "CHK-FBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                targetId,
                ChunkType.FEEDBACK,
                content,
                1.0,
                score,
                emb,
                Instant.now()
        );
    }
}
