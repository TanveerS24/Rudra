package com.spaceweather.backend.risk;

public class RiskPolicy {
    private final double maxSolarFlareWeight = 20.0;
    private final double maxGeomagneticWeight = 25.0;
    private final double maxRadiationWeight = 20.0;
    private final double maxSolarWindWeight = 15.0;
    private final double maxSatelliteVulnWeight = 20.0;

    public double getMaxSolarFlareWeight() { return maxSolarFlareWeight; }
    public double getMaxGeomagneticWeight() { return maxGeomagneticWeight; }
    public double getMaxRadiationWeight() { return maxRadiationWeight; }
    public double getMaxSolarWindWeight() { return maxSolarWindWeight; }
    public double getMaxSatelliteVulnWeight() { return maxSatelliteVulnWeight; }
}
