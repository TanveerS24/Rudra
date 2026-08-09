package com.spaceweather.backend.config;

import com.spaceweather.shared.util.StructuredLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionPool implements AutoCloseable {
    private static final StructuredLogger log = StructuredLogger.of(DatabaseConnectionPool.class, "MAIN-BACKEND");
    private final HikariDataSource dataSource;
    private final boolean connected;

    public DatabaseConnectionPool(AppConfig config) {
        HikariDataSource ds = null;
        boolean isConnected = false;
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getDbUrl());
            hikariConfig.setUsername(config.getDbUser());
            hikariConfig.setPassword(config.getDbPassword());
            hikariConfig.setMaximumPoolSize(config.getDbMaxPoolSize());
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(3000); // 3 seconds timeout for fast fallback
            hikariConfig.setValidationTimeout(2000);
            hikariConfig.setPoolName("SpaceWeather-HikariPool");

            ds = new HikariDataSource(hikariConfig);
            // Test connection
            try (Connection conn = ds.getConnection()) {
                if (conn.isValid(2)) {
                    isConnected = true;
                    log.info("Successfully connected to MySQL database at: {}", config.getDbUrl());
                }
            }
        } catch (Exception e) {
            log.warn("Could not connect to MySQL database: {}. Application will run in memory-resilient mode.", e.getMessage());
            if (ds != null) {
                try { ds.close(); } catch (Exception ignored) {}
                ds = null;
            }
            isConnected = false;
        }
        this.dataSource = ds;
        this.connected = isConnected;
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || !connected) {
            throw new SQLException("Database connection pool is not available or disconnected.");
        }
        return dataSource.getConnection();
    }

    public boolean isConnected() {
        return connected && dataSource != null && !dataSource.isClosed();
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Database connection pool closed.");
        }
    }
}
