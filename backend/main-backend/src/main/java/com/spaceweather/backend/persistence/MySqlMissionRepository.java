package com.spaceweather.backend.persistence;

import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.shared.model.Mission;
import com.spaceweather.shared.util.StructuredLogger;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MySqlMissionRepository implements MissionRepository {
    private static final StructuredLogger log = StructuredLogger.of(MySqlMissionRepository.class, "MAIN-BACKEND");
    private final DatabaseConnectionPool pool;
    private final Map<String, Mission> memoryCache = new ConcurrentHashMap<>();

    public MySqlMissionRepository(DatabaseConnectionPool pool) {
        this.pool = pool;
        seedDefaultMissions();
    }

    private void seedDefaultMissions() {
        List<Mission> list = List.of(
                new Mission("MIS-001", "Artemis Lunar Relay Ops", "CRITICAL", "ACTIVE", "SAT-002-ISS", Instant.now(), null, Instant.now()),
                new Mission("MIS-002", "Global Climate Topography Scan", "HIGH", "ACTIVE", "SAT-003-SENT6", Instant.now(), null, Instant.now()),
                new Mission("MIS-003", "Deep Space Optical Astronomy Phase 4", "MEDIUM", "ACTIVE", "SAT-006-HST", Instant.now(), null, Instant.now())
        );
        for (Mission m : list) {
            memoryCache.put(m.getMissionId(), m);
        }
    }

    @Override
    public Mission save(Mission mission) {
        Objects.requireNonNull(mission, "mission cannot be null");
        memoryCache.put(mission.getMissionId(), mission);
        if (pool.isConnected()) {
            String sql = "INSERT INTO missions (mission_id, name, priority, status, target_satellite_id, start_time, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE status=VALUES(status)";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, mission.getMissionId());
                ps.setString(2, mission.getName());
                ps.setString(3, mission.getPriority());
                ps.setString(4, mission.getStatus());
                ps.setString(5, mission.getTargetSatelliteId());
                ps.setTimestamp(6, Timestamp.from(mission.getStartTime()));
                ps.setTimestamp(7, Timestamp.from(mission.getCreatedAt()));
                ps.executeUpdate();
            } catch (SQLException e) {
                log.warn("Failed to persist mission in MySQL: {}", e.getMessage());
            }
        }
        return mission;
    }

    @Override
    public Optional<Mission> findById(String missionId) {
        if (missionId == null) return Optional.empty();
        return Optional.ofNullable(memoryCache.get(missionId));
    }

    @Override
    public List<Mission> findActive() {
        List<Mission> list = new ArrayList<>();
        for (Mission m : memoryCache.values()) {
            if ("ACTIVE".equalsIgnoreCase(m.getStatus())) {
                list.add(m);
            }
        }
        return list;
    }

    @Override
    public List<Mission> findAll() {
        return new ArrayList<>(memoryCache.values());
    }
}
