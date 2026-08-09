package com.spaceweather.simulation.llm;

import com.spaceweather.shared.model.EventType;
import com.spaceweather.shared.model.MemoryChunk;
import com.spaceweather.shared.model.SpaceWeatherEvent;

import java.util.List;

public interface LLMClient {
    SpaceWeatherEvent generateScenario(String intensityPreference, EventType typePreference, List<MemoryChunk> contextMemories);
}
