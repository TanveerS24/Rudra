package com.spaceweather.backend.persistence;

import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.shared.model.Recommendation;
import com.spaceweather.shared.util.StructuredLogger;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MySqlRecommendationRepository implements RecommendationRepository {
    private static final StructuredLogger log = StructuredLogger.of(MySqlRecommendationRepository.class, "MAIN-BACKEND");
    private final DatabaseConnectionPool pool;
    private final Map<String, Recommendation> memoryCache = new ConcurrentHashMap<>();
    private final List<String> insertionOrder = Collections.synchronizedList(new ArrayList<>());

    public MySqlRecommendationRepository(DatabaseConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public Recommendation save(Recommendation rec) {
        Objects.requireNonNull(rec, "recommendation cannot be null");
        memoryCache.put(rec.getRecommendationId(), rec);
        if (!insertionOrder.contains(rec.getRecommendationId())) {
            insertionOrder.add(0, rec.getRecommendationId());
        }

        if (pool.isConnected()) {
            String sql = "INSERT INTO recommendations (recommendation_id, event_id, assessment_id, satellite_id, " +
                    "action, reasoning, expected_impact, confidence, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE status=VALUES(status)";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, rec.getRecommendationId());
                ps.setString(2, rec.getEventId());
                ps.setString(3, rec.getAssessmentId());
                ps.setString(4, rec.getSatelliteId());
                ps.setString(5, rec.getAction());
                ps.setString(6, rec.getReasoning());
                ps.setString(7, rec.getExpectedImpact());
                ps.setDouble(8, rec.getConfidence());
                ps.setString(9, rec.getStatus());
                ps.setTimestamp(10, Timestamp.from(rec.getCreatedAt()));
                ps.executeUpdate();
            } catch (SQLException e) {
                log.warn("Failed to persist recommendation {} to MySQL: {}", rec.getRecommendationId(), e.getMessage());
            }
        }
        return rec;
    }

    @Override
    public Optional<Recommendation> findById(String recommendationId) {
        if (recommendationId == null) return Optional.empty();
        return Optional.ofNullable(memoryCache.get(recommendationId));
    }

    @Override
    public List<Recommendation> findByEventId(String eventId) {
        List<Recommendation> list = new ArrayList<>();
        for (Recommendation r : memoryCache.values()) {
            if (eventId.equals(r.getEventId())) {
                list.add(r);
            }
        }
        return list;
    }

    @Override
    public List<Recommendation> findPending() {
        List<Recommendation> list = new ArrayList<>();
        for (Recommendation r : memoryCache.values()) {
            if ("PENDING".equalsIgnoreCase(r.getStatus())) {
                list.add(r);
            }
        }
        return list;
    }

    @Override
    public List<Recommendation> findAll(int limit) {
        int max = Math.max(1, limit);
        List<Recommendation> list = new ArrayList<>();
        synchronized (insertionOrder) {
            for (int i = 0; i < Math.min(max, insertionOrder.size()); i++) {
                Recommendation r = memoryCache.get(insertionOrder.get(i));
                if (r != null) list.add(r);
            }
        }
        return list;
    }

    @Override
    public boolean updateStatus(String recommendationId, String status) {
        Recommendation r = memoryCache.get(recommendationId);
        if (r != null) {
            r.setStatus(status);
        }
        if (pool.isConnected()) {
            String sql = "UPDATE recommendations SET status = ? WHERE recommendation_id = ?";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setString(2, recommendationId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                log.warn("Failed to update recommendation status in MySQL: {}", e.getMessage());
            }
        }
        return r != null;
    }
}
