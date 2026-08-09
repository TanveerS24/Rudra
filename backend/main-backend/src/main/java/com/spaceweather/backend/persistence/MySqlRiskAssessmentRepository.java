package com.spaceweather.backend.persistence;

import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.shared.model.RiskAssessment;
import com.spaceweather.shared.model.RiskLevel;
import com.spaceweather.shared.util.JsonUtils;
import com.spaceweather.shared.util.StructuredLogger;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MySqlRiskAssessmentRepository implements RiskAssessmentRepository {
    private static final StructuredLogger log = StructuredLogger.of(MySqlRiskAssessmentRepository.class, "MAIN-BACKEND");
    private final DatabaseConnectionPool pool;
    private final Map<String, RiskAssessment> memoryCache = new ConcurrentHashMap<>();
    private final List<String> insertionOrder = Collections.synchronizedList(new ArrayList<>());

    public MySqlRiskAssessmentRepository(DatabaseConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public RiskAssessment save(RiskAssessment assessment) {
        Objects.requireNonNull(assessment, "assessment cannot be null");
        memoryCache.put(assessment.getAssessmentId(), assessment);
        if (!insertionOrder.contains(assessment.getAssessmentId())) {
            insertionOrder.add(0, assessment.getAssessmentId());
        }

        if (pool.isConnected()) {
            String sql = "INSERT INTO risk_assessments (assessment_id, event_id, satellite_id, deterministic_score, " +
                    "final_score, risk_level, primary_factors, potential_effects, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE final_score=VALUES(final_score), risk_level=VALUES(risk_level)";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, assessment.getAssessmentId());
                ps.setString(2, assessment.getEventId());
                ps.setString(3, assessment.getSatelliteId());
                ps.setDouble(4, assessment.getDeterministicScore());
                ps.setDouble(5, assessment.getFinalScore());
                ps.setString(6, assessment.getRiskLevel().name());
                ps.setString(7, JsonUtils.toJson(assessment.getPrimaryFactors()));
                ps.setString(8, JsonUtils.toJson(assessment.getPotentialEffects()));
                ps.setTimestamp(9, Timestamp.from(assessment.getCreatedAt()));
                ps.executeUpdate();
            } catch (SQLException e) {
                log.warn("Failed to persist risk assessment {} to MySQL: {}", assessment.getAssessmentId(), e.getMessage());
            }
        }
        return assessment;
    }

    @Override
    public Optional<RiskAssessment> findById(String assessmentId) {
        if (assessmentId == null) return Optional.empty();
        if (pool.isConnected()) {
            String sql = "SELECT assessment_id, event_id, satellite_id, deterministic_score, final_score, " +
                    "risk_level, primary_factors, potential_effects, created_at " +
                    "FROM risk_assessments WHERE assessment_id = ?";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, assessmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            } catch (SQLException e) {
                log.warn("Error querying risk assessment {}: {}", assessmentId, e.getMessage());
            }
        }
        return Optional.ofNullable(memoryCache.get(assessmentId));
    }

    @Override
    public List<RiskAssessment> findByEventId(String eventId) {
        if (eventId == null) return Collections.emptyList();
        if (pool.isConnected()) {
            String sql = "SELECT assessment_id, event_id, satellite_id, deterministic_score, final_score, " +
                    "risk_level, primary_factors, potential_effects, created_at " +
                    "FROM risk_assessments WHERE event_id = ? ORDER BY final_score DESC";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<RiskAssessment> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    if (!list.isEmpty()) return list;
                }
            } catch (SQLException e) {
                log.warn("Error querying risk assessments for event {}: {}", eventId, e.getMessage());
            }
        }
        List<RiskAssessment> list = new ArrayList<>();
        for (RiskAssessment ra : memoryCache.values()) {
            if (eventId.equals(ra.getEventId())) {
                list.add(ra);
            }
        }
        list.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));
        return list;
    }

    @Override
    public List<RiskAssessment> findBySatelliteId(String satelliteId) {
        if (satelliteId == null) return Collections.emptyList();
        List<RiskAssessment> list = new ArrayList<>();
        for (RiskAssessment ra : memoryCache.values()) {
            if (satelliteId.equals(ra.getSatelliteId())) {
                list.add(ra);
            }
        }
        return list;
    }

    @Override
    public List<RiskAssessment> findRecent(int limit) {
        int max = Math.max(1, limit);
        List<RiskAssessment> list = new ArrayList<>();
        synchronized (insertionOrder) {
            for (int i = 0; i < Math.min(max, insertionOrder.size()); i++) {
                RiskAssessment ra = memoryCache.get(insertionOrder.get(i));
                if (ra != null) list.add(ra);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private RiskAssessment mapRow(ResultSet rs) throws SQLException {
        String factorsJson = rs.getString("primary_factors");
        String effectsJson = rs.getString("potential_effects");
        List<String> factors = factorsJson != null ? JsonUtils.fromJson(factorsJson, List.class) : Collections.emptyList();
        List<String> effects = effectsJson != null ? JsonUtils.fromJson(effectsJson, List.class) : Collections.emptyList();

        return new RiskAssessment(
                rs.getString("assessment_id"),
                rs.getString("event_id"),
                rs.getString("satellite_id"),
                rs.getDouble("deterministic_score"),
                rs.getDouble("final_score"),
                RiskLevel.valueOf(rs.getString("risk_level")),
                factors,
                effects,
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
