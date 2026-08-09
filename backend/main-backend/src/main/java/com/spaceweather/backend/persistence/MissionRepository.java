package com.spaceweather.backend.persistence;

import com.spaceweather.shared.model.Mission;
import java.util.List;
import java.util.Optional;

public interface MissionRepository {
    Mission save(Mission mission);
    Optional<Mission> findById(String missionId);
    List<Mission> findActive();
    List<Mission> findAll();
}
