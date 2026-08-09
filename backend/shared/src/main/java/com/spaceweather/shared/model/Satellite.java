package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class Satellite {
    private final String satelliteId;
    private final String name;
    private final String missionType;
    private final OrbitType orbitType;
    private final double altitudeKm;
    private final double inclinationDeg;
    private double latitude;
    private double longitude;
    private HealthStatus healthStatus;
    private final SensitivityLevel radiationSensitivity;
    private final SensitivityLevel communicationSensitivity;
    private final SensitivityLevel navigationSensitivity;
    private OperationalStatus operationalStatus;
    private final Instant createdAt;
    private Instant updatedAt;

    @JsonCreator
    public Satellite(
            @JsonProperty("satelliteId") String satelliteId,
            @JsonProperty("name") String name,
            @JsonProperty("missionType") String missionType,
            @JsonProperty("orbitType") OrbitType orbitType,
            @JsonProperty("altitudeKm") double altitudeKm,
            @JsonProperty("inclinationDeg") double inclinationDeg,
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude,
            @JsonProperty("healthStatus") HealthStatus healthStatus,
            @JsonProperty("radiationSensitivity") SensitivityLevel radiationSensitivity,
            @JsonProperty("communicationSensitivity") SensitivityLevel communicationSensitivity,
            @JsonProperty("navigationSensitivity") SensitivityLevel navigationSensitivity,
            @JsonProperty("operationalStatus") OperationalStatus operationalStatus,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt) {
        this.satelliteId = Objects.requireNonNull(satelliteId, "satelliteId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.missionType = Objects.requireNonNull(missionType, "missionType cannot be null");
        this.orbitType = Objects.requireNonNull(orbitType, "orbitType cannot be null");
        this.altitudeKm = altitudeKm;
        this.inclinationDeg = inclinationDeg;
        this.latitude = latitude;
        this.longitude = longitude;
        this.healthStatus = healthStatus != null ? healthStatus : HealthStatus.NOMINAL;
        this.radiationSensitivity = radiationSensitivity != null ? radiationSensitivity : SensitivityLevel.MEDIUM;
        this.communicationSensitivity = communicationSensitivity != null ? communicationSensitivity : SensitivityLevel.MEDIUM;
        this.navigationSensitivity = navigationSensitivity != null ? navigationSensitivity : SensitivityLevel.MEDIUM;
        this.operationalStatus = operationalStatus != null ? operationalStatus : OperationalStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public String getSatelliteId() { return satelliteId; }
    public String getName() { return name; }
    public String getMissionType() { return missionType; }
    public OrbitType getOrbitType() { return orbitType; }
    public double getAltitudeKm() { return altitudeKm; }
    public double getInclinationDeg() { return inclinationDeg; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public HealthStatus getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatus healthStatus) { this.healthStatus = healthStatus; }
    public SensitivityLevel getRadiationSensitivity() { return radiationSensitivity; }
    public SensitivityLevel getCommunicationSensitivity() { return communicationSensitivity; }
    public SensitivityLevel getNavigationSensitivity() { return navigationSensitivity; }
    public OperationalStatus getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(OperationalStatus operationalStatus) { this.operationalStatus = operationalStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Satellite satellite = (Satellite) o;
        return Objects.equals(satelliteId, satellite.satelliteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(satelliteId);
    }

    @Override
    public String toString() {
        return "Satellite{" +
                "id='" + satelliteId + '\'' +
                ", name='" + name + '\'' +
                ", orbit=" + orbitType +
                ", alt=" + altitudeKm + "km" +
                ", health=" + healthStatus +
                '}';
    }
}
