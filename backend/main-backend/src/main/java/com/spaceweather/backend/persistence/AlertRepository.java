package com.spaceweather.backend.persistence;

import com.spaceweather.shared.model.Alert;
import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    Alert save(Alert alert);
    Optional<Alert> findById(String alertId);
    List<Alert> findActive();
    List<Alert> findAll(int limit);
    boolean acknowledge(String alertId);
}
