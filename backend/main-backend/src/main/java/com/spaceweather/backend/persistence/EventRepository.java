package com.spaceweather.backend.persistence;

import com.spaceweather.shared.model.SpaceWeatherEvent;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
    SpaceWeatherEvent save(SpaceWeatherEvent event);
    Optional<SpaceWeatherEvent> findById(String eventId);
    Optional<SpaceWeatherEvent> findLatest();
    List<SpaceWeatherEvent> findRecent(int limit);
    boolean existsById(String eventId);
    long count();
}
