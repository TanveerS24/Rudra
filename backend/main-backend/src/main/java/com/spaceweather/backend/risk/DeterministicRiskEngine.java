package com.spaceweather.backend.risk;

import com.spaceweather.shared.model.OrbitType;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.model.SpaceWeatherEvent;

public class DeterministicRiskEngine {
    private final RiskPolicy policy;

    public DeterministicRiskEngine(RiskPolicy policy) {
        this.policy = policy != null ? policy : new RiskPolicy();
    }

    public DeterministicRiskEngine() {
        this(new RiskPolicy());
    }

    public double calculateScore(SpaceWeatherEvent event, Satellite satellite) {
        if (event == null || satellite == null) return 0.0;

        double flareScore = calculateFlareScore(event.getIntensity());
        double geoScore = calculateGeomagneticScore(event.getGeomagneticIndex());
        double radScore = calculateRadiationScore(event.getRadiationLevel());
        double windScore = calculateWindScore(event.getSolarWindSpeed());
        double vulnScore = calculateSatelliteVulnerabilityScore(event, satellite);

        double total = flareScore + geoScore + radScore + windScore + vulnScore;
        return Math.max(0.0, Math.min(100.0, Math.round(total * 10.0) / 10.0));
    }

    public double calculateFlareScore(String intensity) {
        if (intensity == null) return 2.0;
        String upper = intensity.toUpperCase().trim();
        if (upper.startsWith("X")) {
            try {
                double val = Double.parseDouble(upper.substring(1));
                return Math.min(policy.getMaxSolarFlareWeight(), 15.0 + Math.min(5.0, val * 1.5));
            } catch (NumberFormatException e) {
                return 18.0;
            }
        } else if (upper.startsWith("M")) {
            try {
                double val = Double.parseDouble(upper.substring(1));
                return Math.min(14.0, 8.0 + Math.min(6.0, val * 0.7));
            } catch (NumberFormatException e) {
                return 10.0;
            }
        } else if (upper.startsWith("C")) {
            return 4.0;
        }
        return 2.0;
    }

    public double calculateGeomagneticScore(int kpIndex) {
        // Kp range 0 to 9
        double normalized = Math.max(0, Math.min(9, kpIndex)) / 9.0;
        return normalized * policy.getMaxGeomagneticWeight();
    }

    public double calculateRadiationScore(String radiationLevel) {
        if (radiationLevel == null) return 2.0;
        return switch (radiationLevel.toUpperCase().trim()) {
            case "EXTREME", "CRITICAL", "S5", "S4" -> policy.getMaxRadiationWeight();
            case "HIGH", "S3" -> 16.0;
            case "ELEVATED", "MODERATE", "S2" -> 10.0;
            case "LOW", "S1" -> 5.0;
            default -> 2.0;
        };
    }

    public double calculateWindScore(double speedKmS) {
        if (speedKmS >= 800.0) return policy.getMaxSolarWindWeight();
        if (speedKmS >= 650.0) return 12.0;
        if (speedKmS >= 500.0) return 8.0;
        if (speedKmS >= 400.0) return 4.0;
        return 2.0;
    }

    public double calculateSatelliteVulnerabilityScore(SpaceWeatherEvent event, Satellite satellite) {
        double baseWeight = 8.0;

        // Sensitivity multipliers
        double radMult = satellite.getRadiationSensitivity().getMultiplier();
        double commMult = satellite.getCommunicationSensitivity().getMultiplier();
        double navMult = satellite.getNavigationSensitivity().getMultiplier();
        double avgSens = (radMult + commMult + navMult) / 3.0;

        // Orbit specific factors
        double orbitFactor = switch (satellite.getOrbitType()) {
            case LEO -> (event.getGeomagneticIndex() >= 6) ? 1.4 : 1.0; // Atmospheric expansion drag
            case MEO -> 1.3; // Trapped Van Allen radiation belts
            case GEO -> 1.2; // Unshielded magnetopause exposure
            case HEO -> 1.5; // High apogee outside magnetosphere
        };

        double vuln = baseWeight * avgSens * (orbitFactor * 0.7);
        return Math.min(policy.getMaxSatelliteVulnWeight(), vuln);
    }
}
