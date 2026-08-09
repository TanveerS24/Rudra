package com.spaceweather.simulation.queue;

import com.spaceweather.shared.model.EventType;
import com.spaceweather.shared.model.SpaceWeatherEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SimulationTask {
    private final String taskId;
    private final String intensityPreference;
    private final EventType typePreference;
    private final CompletableFuture<SpaceWeatherEvent> future;

    public SimulationTask(String intensityPreference, EventType typePreference) {
        this.taskId = "TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.intensityPreference = intensityPreference;
        this.typePreference = typePreference;
        this.future = new CompletableFuture<>();
    }

    public String getTaskId() { return taskId; }
    public String getIntensityPreference() { return intensityPreference; }
    public EventType getTypePreference() { return typePreference; }
    public CompletableFuture<SpaceWeatherEvent> getFuture() { return future; }
}
