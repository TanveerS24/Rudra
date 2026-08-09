package com.spaceweather.simulation.chunking;

import com.spaceweather.shared.model.MemoryChunk;
import com.spaceweather.shared.model.SpaceWeatherEvent;

public interface SemanticChunker {
    MemoryChunk chunkEvent(SpaceWeatherEvent event);
    MemoryChunk chunkEnvironment(SpaceWeatherEvent event);
    MemoryChunk chunkGeographicImpact(SpaceWeatherEvent event);
    MemoryChunk chunkSatelliteImpact(SpaceWeatherEvent event);
    MemoryChunk chunkOperationalImpact(SpaceWeatherEvent event);
    MemoryChunk chunkRecommendation(String eventId, String action, String reasoning);
    MemoryChunk chunkFeedback(String targetId, double score, String feedbackText);
}
