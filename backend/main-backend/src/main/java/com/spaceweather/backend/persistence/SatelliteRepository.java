package com.spaceweather.backend.persistence;

import com.spaceweather.shared.model.HealthStatus;
import com.spaceweather.shared.model.OperationalStatus;
import com.spaceweather.shared.model.Satellite;

import java.util.List;
import java.util.Optional;

public interface SatelliteRepository {
    Satellite save(Satellite satellite);
    Optional<Satellite> findById(String satelliteId);
    List<Satellite> findAll();
    boolean updateHealthAndStatus(String satelliteId, HealthStatus healthStatus, OperationalStatus operationalStatus);
    long count();
}
