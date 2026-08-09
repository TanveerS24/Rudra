package com.spaceweather.shared.util;

import com.spaceweather.shared.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    @DisplayName("Should serialize and deserialize SpaceWeatherEvent with ISO Instant and Location")
    void testSpaceWeatherEventSerialization() {
        SpaceWeatherEvent event = new SpaceWeatherEvent(
                "EVT-TEST-101",
                Instant.parse("2026-08-09T10:00:00Z"),
                EventType.SOLAR_FLARE,
                "X2.4",
                45,
                650.0,
                7,
                "HIGH",
                new Location(12.4, 45.2),
                List.of("North America", "Polar Cap"),
                new Location(13.08, 80.27),
                "Strong flare observed",
                0.95
        );

        String json = JsonUtils.toJson(event);
        assertNotNull(json);
        assertTrue(json.contains("EVT-TEST-101"));
        assertTrue(json.contains("SOLAR_FLARE"));

        SpaceWeatherEvent deserialized = JsonUtils.fromJson(json, SpaceWeatherEvent.class);
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getEventType(), deserialized.getEventType());
        assertEquals(event.getIntensity(), deserialized.getIntensity());
        assertEquals(event.getGeomagneticIndex(), deserialized.getGeomagneticIndex());
        assertEquals(event.getSolarWindSpeed(), deserialized.getSolarWindSpeed());
        assertEquals(event.getOrigin().getLatitude(), deserialized.getOrigin().getLatitude());
        assertEquals(2, deserialized.getAffectedRegions().size());
    }

    @Test
    @DisplayName("Should serialize and deserialize Satellite entity")
    void testSatelliteSerialization() {
        Satellite sat = new Satellite(
                "SAT-001",
                "ISS Alpha",
                "Human Spaceflight",
                OrbitType.LEO,
                420.0,
                51.6,
                20.0,
                40.0,
                HealthStatus.NOMINAL,
                SensitivityLevel.HIGH,
                SensitivityLevel.CRITICAL,
                SensitivityLevel.MEDIUM,
                OperationalStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );

        String json = JsonUtils.toJson(sat);
        Satellite deserialized = JsonUtils.fromJson(json, Satellite.class);
        assertEquals(sat.getSatelliteId(), deserialized.getSatelliteId());
        assertEquals(sat.getOrbitType(), deserialized.getOrbitType());
        assertEquals(sat.getRadiationSensitivity(), deserialized.getRadiationSensitivity());
    }
}
