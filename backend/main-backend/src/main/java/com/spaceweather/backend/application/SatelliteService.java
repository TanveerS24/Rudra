package com.spaceweather.backend.application;

import com.spaceweather.backend.persistence.SatelliteRepository;
import com.spaceweather.backend.websocket.SpaceWeatherWebSocketServer;
import com.spaceweather.shared.dto.SatelliteDTO;
import com.spaceweather.shared.model.HealthStatus;
import com.spaceweather.shared.model.OperationalStatus;
import com.spaceweather.shared.model.Satellite;
import com.spaceweather.shared.util.CorrelationContext;
import com.spaceweather.shared.util.StructuredLogger;

import java.util.List;
import java.util.Optional;

public class SatelliteService {
    private static final StructuredLogger log = StructuredLogger.of(SatelliteService.class, "MAIN-BACKEND");
    private final SatelliteRepository satelliteRepository;
    private final SpaceWeatherWebSocketServer wsServer;

    public SatelliteService(SatelliteRepository satelliteRepository, SpaceWeatherWebSocketServer wsServer) {
        this.satelliteRepository = satelliteRepository;
        this.wsServer = wsServer;
    }

    public List<Satellite> getAllSatellites() {
        return satelliteRepository.findAll();
    }

    public Optional<Satellite> getSatelliteById(String satelliteId) {
        return satelliteRepository.findById(satelliteId);
    }

    public boolean updateSatelliteStatus(String satelliteId, String healthStr, String operationalStr) {
        HealthStatus health = healthStr != null ? HealthStatus.valueOf(healthStr.toUpperCase()) : null;
        OperationalStatus status = operationalStr != null ? OperationalStatus.valueOf(operationalStr.toUpperCase()) : null;

        boolean updated = satelliteRepository.updateHealthAndStatus(satelliteId, health, status);
        if (updated && wsServer != null) {
            List<Satellite> list = satelliteRepository.findAll();
            wsServer.broadcastMessage("SATELLITE_STATUS_CHANGED", list.stream().map(this::toDTO).toList(), CorrelationContext.getRequestId());
        }
        return updated;
    }

    public SatelliteDTO toDTO(Satellite sat) {
        if (sat == null) return null;
        return new SatelliteDTO(
                sat.getSatelliteId(),
                sat.getName(),
                sat.getMissionType(),
                sat.getOrbitType().name(),
                sat.getAltitudeKm(),
                sat.getInclinationDeg(),
                sat.getLatitude(),
                sat.getLongitude(),
                sat.getHealthStatus().name(),
                sat.getRadiationSensitivity().name(),
                sat.getCommunicationSensitivity().name(),
                sat.getNavigationSensitivity().name(),
                sat.getOperationalStatus().name(),
                sat.getCreatedAt(),
                sat.getUpdatedAt()
        );
    }
}
