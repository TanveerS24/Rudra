package com.spaceweather.backend.persistence;

import com.spaceweather.shared.model.RiskAssessment;
import java.util.List;
import java.util.Optional;

public interface RiskAssessmentRepository {
    RiskAssessment save(RiskAssessment assessment);
    Optional<RiskAssessment> findById(String assessmentId);
    List<RiskAssessment> findByEventId(String eventId);
    List<RiskAssessment> findBySatelliteId(String satelliteId);
    List<RiskAssessment> findRecent(int limit);
}
