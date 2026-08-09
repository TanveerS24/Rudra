package com.spaceweather.backend.persistence;

import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.shared.model.*;
import com.spaceweather.shared.util.StructuredLogger;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MySqlSatelliteRepository implements SatelliteRepository {
    private static final StructuredLogger log = StructuredLogger.of(MySqlSatelliteRepository.class, "MAIN-BACKEND");
    private final DatabaseConnectionPool pool;
    private final Map<String, Satellite> memoryCache = new ConcurrentHashMap<>();

    public MySqlSatelliteRepository(DatabaseConnectionPool pool) {
        this.pool = pool;
        seedDefaultSatellites();
    }

    private void seedDefaultSatellites() {
        List<Satellite> seeds = List.of(
                new Satellite("SAT-001-GOES18", "NOAA GOES-18 Weather Monitor", "Earth Observation", OrbitType.GEO, 35786.0, 0.05, 0.0, -137.2, HealthStatus.NOMINAL, SensitivityLevel.MEDIUM, SensitivityLevel.HIGH, SensitivityLevel.LOW, OperationalStatus.ACTIVE, Instant.now(), Instant.now()),
                new Satellite("SAT-002-ISS", "ISS Alpha Station", "Human Spaceflight", OrbitType.LEO, 418.0, 51.64, 25.4, 45.1, HealthStatus.NOMINAL, SensitivityLevel.HIGH, SensitivityLevel.HIGH, SensitivityLevel.HIGH, OperationalStatus.ACTIVE, Instant.now(), Instant.now()),
                new Satellite("SAT-003-SENT6", "Sentinel-6 Michael Freilich", "Ocean Topography", OrbitType.LEO, 1336.0, 66.04, -32.1, 115.3, HealthStatus.NOMINAL, SensitivityLevel.HIGH, SensitivityLevel.MEDIUM, SensitivityLevel.CRITICAL, OperationalStatus.ACTIVE, Instant.now(), Instant.now()),
                new Satellite("SAT-004-STAR412", "Starlink Group 4-12 Constellation Leader", "Broadband Relay", OrbitType.LEO, 540.0, 53.22, 48.2, -122.4, HealthStatus.NOMINAL, SensitivityLevel.MEDIUM, SensitivityLevel.CRITICAL, SensitivityLevel.MEDIUM, OperationalStatus.ACTIVE, Instant.now(), Instant.now()),
                new Satellite("SAT-005-GPS06", "NAVSTAR GPS-III SV06", "Global Positioning", OrbitType.MEO, 20180.0, 55.0, 12.8, -45.6, HealthStatus.NOMINAL, SensitivityLevel.HIGH, SensitivityLevel.HIGH, SensitivityLevel.CRITICAL, OperationalStatus.ACTIVE, Instant.now(), Instant.now()),
                new Satellite("SAT-006-HST", "Hubble Space Telescope", "Deep Astronomy", OrbitType.LEO, 535.0, 28.47, -18.4, -65.2, HealthStatus.NOMINAL, SensitivityLevel.CRITICAL, SensitivityLevel.MEDIUM, SensitivityLevel.LOW, OperationalStatus.ACTIVE, Instant.now(), Instant.now()),
                new Satellite("SAT-007-GAL24", "Galileo FOC FM24 Elena", "Navigation & Timing", OrbitType.MEO, 23222.0, 56.0, -4.2, 78.9, HealthStatus.NOMINAL, SensitivityLevel.HIGH, SensitivityLevel.HIGH, SensitivityLevel.CRITICAL, OperationalStatus.ACTIVE, Instant.now(), Instant.now()),
                new Satellite("SAT-008-CHANDRA", "Chandra X-Ray Observatory", "Astrophysics", OrbitType.HEO, 64000.0, 28.5, 34.5, 142.1, HealthStatus.NOMINAL, SensitivityLevel.CRITICAL, SensitivityLevel.HIGH, SensitivityLevel.MEDIUM, OperationalStatus.ACTIVE, Instant.now(), Instant.now())
        );
        for (Satellite s : seeds) {
            memoryCache.put(s.getSatelliteId(), s);
        }
    }

    @Override
    public Satellite save(Satellite satellite) {
        Objects.requireNonNull(satellite, "satellite cannot be null");
        memoryCache.put(satellite.getSatelliteId(), satellite);

        if (pool.isConnected()) {
            String sql = "INSERT INTO satellites (satellite_id, name, mission_type, orbit_type, altitude_km, inclination_deg, " +
                    "latitude, longitude, health_status, radiation_sensitivity, communication_sensitivity, navigation_sensitivity, " +
                    "operational_status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE latitude=VALUES(latitude), longitude=VALUES(longitude), " +
                    "health_status=VALUES(health_status), operational_status=VALUES(operational_status), updated_at=VALUES(updated_at)";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, satellite.getSatelliteId());
                ps.setString(2, satellite.getName());
                ps.setString(3, satellite.getMissionType());
                ps.setString(4, satellite.getOrbitType().name());
                ps.setDouble(5, satellite.getAltitudeKm());
                ps.setDouble(6, satellite.getInclinationDeg());
                ps.setDouble(7, satellite.getLatitude());
                ps.setDouble(8, satellite.getLongitude());
                ps.setString(9, satellite.getHealthStatus().name());
                ps.setString(10, satellite.getRadiationSensitivity().name());
                ps.setString(11, satellite.getCommunicationSensitivity().name());
                ps.setString(12, satellite.getNavigationSensitivity().name());
                ps.setString(13, satellite.getOperationalStatus().name());
                ps.setTimestamp(14, Timestamp.from(satellite.getCreatedAt()));
                ps.setTimestamp(15, Timestamp.from(satellite.getUpdatedAt()));
                ps.executeUpdate();
            } catch (SQLException e) {
                log.warn("Failed to persist satellite {} to MySQL: {}", satellite.getSatelliteId(), e.getMessage());
            }
        }
        return satellite;
    }

    @Override
    public Optional<Satellite> findById(String satelliteId) {
        if (satelliteId == null) return Optional.empty();
        if (pool.isConnected()) {
            String sql = "SELECT satellite_id, name, mission_type, orbit_type, altitude_km, inclination_deg, " +
                    "latitude, longitude, health_status, radiation_sensitivity, communication_sensitivity, " +
                    "navigation_sensitivity, operational_status, created_at, updated_at " +
                    "FROM satellites WHERE satellite_id = ?";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, satelliteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            } catch (SQLException e) {
                log.warn("Error querying satellite {}: {}", satelliteId, e.getMessage());
            }
        }
        return Optional.ofNullable(memoryCache.get(satelliteId));
    }

    @Override
    public List<Satellite> findAll() {
        if (pool.isConnected()) {
            String sql = "SELECT satellite_id, name, mission_type, orbit_type, altitude_km, inclination_deg, " +
                    "latitude, longitude, health_status, radiation_sensitivity, communication_sensitivity, " +
                    "navigation_sensitivity, operational_status, created_at, updated_at " +
                    "FROM satellites ORDER BY altitude_km ASC";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<Satellite> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException e) {
                log.warn("Error querying all satellites from MySQL: {}", e.getMessage());
            }
        }
        return new ArrayList<>(memoryCache.values());
    }

    @Override
    public boolean updateHealthAndStatus(String satelliteId, HealthStatus healthStatus, OperationalStatus operationalStatus) {
        Satellite sat = memoryCache.get(satelliteId);
        if (sat != null) {
            if (healthStatus != null) sat.setHealthStatus(healthStatus);
            if (operationalStatus != null) sat.setOperationalStatus(operationalStatus);
            sat.setUpdatedAt(Instant.now());
        }

        if (pool.isConnected()) {
            String sql = "UPDATE satellites SET health_status = COALESCE(?, health_status), " +
                    "operational_status = COALESCE(?, operational_status), updated_at = ? WHERE satellite_id = ?";
            try (Connection conn = pool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, healthStatus != null ? healthStatus.name() : null);
                ps.setString(2, operationalStatus != null ? operationalStatus.name() : null);
                ps.setTimestamp(3, Timestamp.from(Instant.now()));
                ps.setString(4, satelliteId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                log.warn("Failed to update satellite health/status in MySQL: {}", e.getMessage());
            }
        }
        return sat != null;
    }

    @Override
    public long count() {
        return memoryCache.size();
    }

    private Satellite mapRow(ResultSet rs) throws SQLException {
        return new Satellite(
                rs.getString("satellite_id"),
                rs.getString("name"),
                rs.getString("mission_type"),
                OrbitType.valueOf(rs.getString("orbit_type")),
                rs.getDouble("altitude_km"),
                rs.getDouble("inclination_deg"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                HealthStatus.valueOf(rs.getString("health_status")),
                SensitivityLevel.valueOf(rs.getString("radiation_sensitivity")),
                SensitivityLevel.valueOf(rs.getString("communication_sensitivity")),
                SensitivityLevel.valueOf(rs.getString("navigation_sensitivity")),
                OperationalStatus.valueOf(rs.getString("operational_status")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
