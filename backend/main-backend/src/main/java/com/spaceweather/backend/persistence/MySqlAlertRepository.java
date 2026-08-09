package com.spaceweather.backend.persistence;

import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.shared.model.Alert;
import com.spaceweather.shared.util.StructuredLogger;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MySqlAlertRepository implements AlertRepository {
    private static final StructuredLogger log = StructuredLogger.of(MySqlAlertRepository.class, "MAIN-BACKEND");
    private final DatabaseConnectionPool pool;
    private final Map<String, Alert> memoryCache = new ConcurrentHashMap<>();
    private final List<String> insertionOrder = Collections.synchronizedList(new ArrayList<>());

    public MySqlAlertRepository(DatabaseConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public Alert save(Alert alert) {
        Objects.requireNonNull(alert, "alert cannot be null");
        memoryCache.put(alert.getAlertId(), alert);
        if (!insertionOrder.contains(alert.getAlertId())) {
            insertionOrder.add(0, alert.getAlertId());
        }

        if (pool.isConnected()) {
            String sql = "INSERT INTO alerts (alert_id, event_id, severity, title, message, acknowledged, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE acknowledged=VALUES(acknowledged)";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, alert.getAlertId());
                ps.setString(2, alert.getEventId());
                ps.setString(3, alert.getSeverity());
                ps.setString(4, alert.getTitle());
                ps.setString(5, alert.getMessage());
                ps.setBoolean(6, alert.isAcknowledged());
                ps.setTimestamp(7, Timestamp.from(alert.getCreatedAt()));
                ps.executeUpdate();
            } catch (SQLException e) {
                log.warn("Failed to persist alert {} to MySQL: {}", alert.getAlertId(), e.getMessage());
            }
        }
        return alert;
    }

    @Override
    public Optional<Alert> findById(String alertId) {
        if (alertId == null) return Optional.empty();
        return Optional.ofNullable(memoryCache.get(alertId));
    }

    @Override
    public List<Alert> findActive() {
        List<Alert> list = new ArrayList<>();
        for (Alert a : memoryCache.values()) {
            if (!a.isAcknowledged()) {
                list.add(a);
            }
        }
        return list;
    }

    @Override
    public List<Alert> findAll(int limit) {
        int max = Math.max(1, limit);
        List<Alert> list = new ArrayList<>();
        synchronized (insertionOrder) {
            for (int i = 0; i < Math.min(max, insertionOrder.size()); i++) {
                Alert a = memoryCache.get(insertionOrder.get(i));
                if (a != null) list.add(a);
            }
        }
        return list;
    }

    @Override
    public boolean acknowledge(String alertId) {
        Alert a = memoryCache.get(alertId);
        if (a != null) {
            a.setAcknowledged(true);
        }
        if (pool.isConnected()) {
            String sql = "UPDATE alerts SET acknowledged = TRUE WHERE alert_id = ?";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, alertId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                log.warn("Failed to acknowledge alert in MySQL: {}", e.getMessage());
            }
        }
        return a != null;
    }
}
