package com.spaceweather.simulation.llm;

import com.spaceweather.shared.model.EventType;
import com.spaceweather.shared.model.Location;
import com.spaceweather.shared.model.SpaceWeatherEvent;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DeterministicScenarioGenerator {
    private static final List<String> REGION_POOL = List.of(
            "North America Subauroral", "North Atlantic Air Routes", "Arctic Polar Cap",
            "Northern Europe", "Scandinavian Power Grid Zone", "High-Latitude LEO Corridors",
            "Australasia Navigation Sectors", "Southern Ocean Air Space", "Antarctic Research Corridors",
            "East Asia Trans-Pacific Routes", "South Atlantic Magnetic Anomaly", "Equatorial Ionospheric Anomaly"
    );

    public SpaceWeatherEvent generate(String intensityPreference, EventType typePreference) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        EventType eventType = typePreference != null ? typePreference :
                EventType.values()[rand.nextInt(EventType.values().length)];

        String intensity;
        int durationMinutes;
        double solarWindSpeed;
        int geomagneticIndex;
        String radiationLevel;
        double confidence = 0.85 + (rand.nextDouble() * 0.12);

        String mode = (intensityPreference != null && !intensityPreference.isBlank()) ?
                intensityPreference.toUpperCase().trim() : "RANDOM";

        switch (mode) {
            case "CRITICAL" -> {
                intensity = "X" + String.format(Locale.US, "%.1f", 2.0 + rand.nextDouble() * 7.0);
                durationMinutes = rand.nextInt(45, 180);
                solarWindSpeed = rand.nextDouble(750.0, 1100.0);
                geomagneticIndex = rand.nextInt(8, 10);
                radiationLevel = "CRITICAL";
            }
            case "HIGH" -> {
                intensity = rand.nextBoolean() ?
                        "X" + String.format(Locale.US, "%.1f", 1.0 + rand.nextDouble() * 1.5) :
                        "M" + String.format(Locale.US, "%.1f", 5.0 + rand.nextDouble() * 4.5);
                durationMinutes = rand.nextInt(30, 90);
                solarWindSpeed = rand.nextDouble(600.0, 780.0);
                geomagneticIndex = rand.nextInt(6, 8);
                radiationLevel = "HIGH";
            }
            case "LOW" -> {
                intensity = "C" + String.format(Locale.US, "%.1f", 1.0 + rand.nextDouble() * 8.0);
                durationMinutes = rand.nextInt(15, 45);
                solarWindSpeed = rand.nextDouble(350.0, 480.0);
                geomagneticIndex = rand.nextInt(1, 4);
                radiationLevel = "NORMAL";
            }
            default -> { // MODERATE / RANDOM
                intensity = "M" + String.format(Locale.US, "%.1f", 1.0 + rand.nextDouble() * 4.5);
                durationMinutes = rand.nextInt(20, 60);
                solarWindSpeed = rand.nextDouble(480.0, 620.0);
                geomagneticIndex = rand.nextInt(4, 6);
                radiationLevel = "ELEVATED";
            }
        }

        // Realistic solar coordinates and terrestrial impact coordinates
        double originLat = rand.nextDouble(-35.0, 35.0);
        double originLon = rand.nextDouble(-85.0, 85.0);
        double impactLat = rand.nextDouble(-65.0, 65.0);
        double impactLon = rand.nextDouble(-170.0, 170.0);

        List<String> affected = new ArrayList<>();
        int regionCount = Math.max(1, (geomagneticIndex / 2));
        Set<Integer> chosen = new HashSet<>();
        while (chosen.size() < regionCount) {
            int idx = rand.nextInt(REGION_POOL.size());
            if (chosen.add(idx)) {
                affected.add(REGION_POOL.get(idx));
            }
        }

        String description = String.format(
                "%s event with intensity %s originating from solar coordinates (%.1f°, %.1f°). Modeled solar wind at %.0f km/s with planetary Kp index %d.",
                eventType.name().replace('_', ' '), intensity, originLat, originLon, solarWindSpeed, geomagneticIndex
        );

        String eventId = "EVT-SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new SpaceWeatherEvent(
                eventId,
                Instant.now(),
                eventType,
                intensity,
                durationMinutes,
                Math.round(solarWindSpeed * 10.0) / 10.0,
                geomagneticIndex,
                radiationLevel,
                new Location(Math.round(originLat * 100.0) / 100.0, Math.round(originLon * 100.0) / 100.0),
                affected,
                new Location(Math.round(impactLat * 100.0) / 100.0, Math.round(impactLon * 100.0) / 100.0),
                description,
                Math.round(confidence * 100.0) / 100.0
        );
    }
}
