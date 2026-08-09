package com.spaceweather.simulation.chunking;

import com.spaceweather.simulation.config.SimulationServiceConfig;
import com.spaceweather.simulation.memory.EmbeddingService;
import com.spaceweather.shared.model.ChunkType;
import com.spaceweather.shared.model.EventType;
import com.spaceweather.shared.model.Location;
import com.spaceweather.shared.model.MemoryChunk;
import com.spaceweather.shared.model.SpaceWeatherEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpaceWeatherSemanticChunkerTest {
    private SpaceWeatherSemanticChunker chunker;

    @BeforeEach
    void setUp() {
        EmbeddingService embeddingService = new EmbeddingService(new SimulationServiceConfig());
        chunker = new SpaceWeatherSemanticChunker(embeddingService);
    }

    @Test
    @DisplayName("Should generate semantic chunks across all 7 categories with embeddings")
    void testSemanticChunkingAllCategories() {
        SpaceWeatherEvent event = new SpaceWeatherEvent(
                "EVT-CHK-01", Instant.now(), EventType.SOLAR_FLARE, "X2.5",
                45, 620.0, 7, "HIGH", new Location(12.0, 45.0),
                List.of("North America", "Polar Cap"), new Location(28.0, -80.0),
                "Major solar flare impacting ionosphere", 0.94
        );

        MemoryChunk cEvent = chunker.chunkEvent(event);
        assertEquals(ChunkType.EVENT, cEvent.getChunkType());
        assertFalse(cEvent.getEmbedding().isEmpty());
        assertTrue(cEvent.getContent().contains("X2.5"));

        MemoryChunk cEnv = chunker.chunkEnvironment(event);
        assertEquals(ChunkType.ENVIRONMENT, cEnv.getChunkType());
        assertTrue(cEnv.getContent().contains("620.0 km/s"));

        MemoryChunk cGeo = chunker.chunkGeographicImpact(event);
        assertEquals(ChunkType.GEOGRAPHIC_IMPACT, cGeo.getChunkType());
        assertTrue(cGeo.getContent().contains("North America"));

        MemoryChunk cSat = chunker.chunkSatelliteImpact(event);
        assertEquals(ChunkType.SATELLITE_IMPACT, cSat.getChunkType());

        MemoryChunk cOps = chunker.chunkOperationalImpact(event);
        assertEquals(ChunkType.OPERATIONAL_IMPACT, cOps.getChunkType());

        MemoryChunk cRec = chunker.chunkRecommendation("EVT-CHK-01", "Orient solar arrays", "Reduce orbital drag");
        assertEquals(ChunkType.RECOMMENDATION, cRec.getChunkType());

        MemoryChunk cFbk = chunker.chunkFeedback("EVT-CHK-01", 0.95, "Accurate prediction");
        assertEquals(ChunkType.FEEDBACK, cFbk.getChunkType());
    }
}
