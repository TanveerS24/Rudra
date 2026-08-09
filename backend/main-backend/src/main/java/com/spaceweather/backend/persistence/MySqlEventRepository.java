package com.spaceweather.backend.persistence;

import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.shared.model.EventType;
import com.spaceweather.shared.model.Location;
import com.spaceweather.shared.model.SpaceWeatherEvent;
import com.spaceweather.shared.util.JsonUtils;
import com.spaceweather.shared.util.StructuredLogger;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MySqlEventRepository implements EventRepository {
    private static final StructuredLogger log = StructuredLogger.of(MySqlEventRepository.class, "MAIN-BACKEND");
    private final DatabaseConnectionPool pool;
    private final Map<String, SpaceWeatherEvent> memoryCache = new ConcurrentHashMap<>();
    private final List<String> insertionOrder = Collections.synchronizedList(new ArrayList<>());

    public MySqlEventRepository(DatabaseConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public SpaceWeatherEvent save(SpaceWeatherEvent event) {
        Objects.requireNonNull(event, "event cannot be null");
        memoryCache.put(event.getEventId(), event);
        if (!insertionOrder.contains(event.getEventId())) {
            insertionOrder.add(0, event.getEventId());
        }

        if (pool.isConnected()) {
            String sql = "INSERT INTO space_weather_events (event_id, event_type, intensity, duration_minutes, " +
                    "solar_wind_speed, geomagnetic_index, radiation_level, origin_latitude, origin_longitude, " +
                    "impact_latitude, impact_longitude, affected_regions, impact_description, confidence, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE intensity=VALUES(intensity), solar_wind_speed=VALUES(solar_wind_speed), " +
                    "geomagnetic_index=VALUES(geomagnetic_index), radiation_level=VALUES(radiation_level)";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, event.getEventId());
                ps.setString(2, event.getEventType().name());
                ps.setString(3, event.getIntensity());
                ps.setInt(4, event.getDurationMinutes());
                ps.setDouble(5, event.getSolarWindSpeed());
                ps.setInt(6, event.getGeomagneticIndex());
                ps.setString(7, event.getRadiationLevel());
                ps.setDouble(8, event.getOrigin().getLatitude());
                ps.setDouble(9, event.getOrigin().getLongitude());
                ps.setDouble(10, event.getMaximumImpactLocation().getLatitude());
                ps.setDouble(11, event.getMaximumImpactLocation().getLongitude());
                ps.setString(12, JsonUtils.toJson(event.getAffectedRegions()));
                ps.setString(13, event.getImpactDescription());
                ps.setDouble(14, event.getConfidence());
                ps.setTimestamp(15, Timestamp.from(event.getTimestamp()));
                ps.executeUpdate();
            } catch (SQLException e) {
                log.warn("Failed to persist event to MySQL: {}. Retaining in memory cache.", e.getMessage());
            }
        }
        return event;
    }

    @Override
    public Optional<SpaceWeatherEvent> findById(String eventId) {
        if (eventId == null) return Optional.empty();
        if (pool.isConnected()) {
            String sql = "SELECT event_id, event_type, intensity, duration_minutes, solar_wind_speed, " +
                    "geomagnetic_index, radiation_level, origin_latitude, origin_longitude, " +
                    "impact_latitude, impact_longitude, affected_regions, impact_description, confidence, created_at " +
                    "FROM space_weather_events WHERE event_id = ?";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            } catch (SQLException e) {
                log.warn("Error querying MySQL for event {}: {}", eventId, e.getMessage());
            }
        }
        return Optional.ofNullable(memoryCache.get(eventId));
    }

    @Override
    public Optional<SpaceWeatherEvent> findLatest() {
        if (pool.isConnected()) {
            String sql = "SELECT event_id, event_type, intensity, duration_minutes, solar_wind_speed, " +
                    "geomagnetic_index, radiation_level, origin_latitude, origin_longitude, " +
                    "impact_latitude, impact_longitude, affected_regions, impact_description, confidence, created_at " +
                    "FROM space_weather_events ORDER BY created_at DESC LIMIT 1";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            } catch (SQLException e) {
                log.warn("Error querying MySQL for latest event: {}", e.getMessage());
            }
        }
        if (!insertionOrder.isEmpty()) {
            String latestId = insertionOrder.get(0);
            return Optional.ofNullable(memoryCache.get(latestId));
        }
        return Optional.empty();
    }

    @Override
    public List<SpaceWeatherEvent> findRecent(int limit) {
        int max = Math.max(1, limit);
        if (pool.isConnected()) {
            String sql = "SELECT event_id, event_type, intensity, duration_minutes, solar_wind_speed, " +
                    "geomagnetic_index, radiation_level, origin_latitude, origin_longitude, " +
                    "impact_latitude, impact_longitude, affected_regions, impact_description, confidence, created_at " +
                    "FROM space_weather_events ORDER BY created_at DESC LIMIT ?";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, max);
                try (ResultSet rs = ps.executeQuery()) {
                    List<SpaceWeatherEvent> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    if (!list.isEmpty()) return list;
                }
            } catch (SQLException e) {
                log.warn("Error querying MySQL for recent events: {}", e.getMessage());
            }
        }
        List<SpaceWeatherEvent> list = new ArrayList<>();
        synchronized (insertionOrder) {
            for (int i = 0; i < Math.min(max, insertionOrder.size()); i++) {
                SpaceWeatherEvent evt = memoryCache.get(insertionOrder.get(i));
                if (evt != null) list.add(evt);
            }
        }
        return list;
    }

    @Override
    public boolean existsById(String eventId) {
        if (eventId == null) return false;
        if (memoryCache.containsKey(eventId)) return true;
        if (pool.isConnected()) {
            String sql = "SELECT 1 FROM space_weather_events WHERE event_id = ?";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                log.warn("Error checking event existence: {}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    public long count() {
        if (pool.isConnected()) {
            String sql = "SELECT COUNT(*) FROM space_weather_events";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            } catch (SQLException e) {
                log.warn("Error counting events in MySQL: {}", e.getMessage());
            }
        }
        return memoryCache.size();
    }

    @SuppressWarnings("unchecked")
    private SpaceWeatherEvent mapRow(ResultSet rs) throws SQLException {
        String eventId = rs.getString("event_id");
        EventType eventType = EventType.valueOf(rs.getString("event_type"));
        String intensity = rs.getString("intensity");
        int duration = rs.getInt("duration_minutes");
        double solarWind = rs.getDouble("solar_wind_speed");
        int kp = rs.getInt("geomagnetic_index");
        String radLevel = rs.getString("radiation_level");
        Location origin = new Location(rs.getDouble("origin_latitude"), rs.getDouble("origin_longitude"));
        Location maxImpact = new Location(rs.getDouble("impact_latitude"), rs.getDouble("impact_longitude"));
        String affectedJson = rs.getString("affected_regions");
        List<String> regions = affectedJson != null ? JsonUtils.fromJson(affectedJson, List.class) : Collections.emptyList();
        String desc = rs.getString("impact_description");
        double conf = rs.getDouble("confidence");
        Instant timestamp = rs.getTimestamp("created_at").toInstant();

        return new SpaceWeatherEvent(
                eventId, timestamp, eventType, intensity, duration,
                solarWind, kp, radLevel, origin, regions, maxImpact, desc, conf
        );
    }
}
