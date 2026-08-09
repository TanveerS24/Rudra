package com.spaceweather.backend.persistence;

import com.spaceweather.shared.model.Recommendation;
import java.util.List;
import java.util.Optional;

public interface RecommendationRepository {
    Recommendation save(Recommendation recommendation);
    Optional<Recommendation> findById(String recommendationId);
    List<Recommendation> findByEventId(String eventId);
    List<Recommendation> findPending();
    List<Recommendation> findAll(int limit);
    boolean updateStatus(String recommendationId, String status);
}
