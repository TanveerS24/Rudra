package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class Alert {
    private final String alertId;
    private final String eventId;
    private final String severity; // INFO, WARNING, CRITICAL
    private final String title;
    private final String message;
    private boolean acknowledged;
    private final Instant createdAt;

    @JsonCreator
    public Alert(
            @JsonProperty("alertId") String alertId,
            @JsonProperty("eventId") String eventId,
            @JsonProperty("severity") String severity,
            @JsonProperty("title") String title,
            @JsonProperty("message") String message,
            @JsonProperty("acknowledged") boolean acknowledged,
            @JsonProperty("createdAt") Instant createdAt) {
        this.alertId = Objects.requireNonNull(alertId, "alertId cannot be null");
        this.eventId = eventId;
        this.severity = severity != null ? severity : "INFO";
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.message = Objects.requireNonNull(message, "message cannot be null");
        this.acknowledged = acknowledged;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getAlertId() { return alertId; }
    public String getEventId() { return eventId; }
    public String getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alert alert = (Alert) o;
        return Objects.equals(alertId, alert.alertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId);
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id='" + alertId + '\'' +
                ", severity='" + severity + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
