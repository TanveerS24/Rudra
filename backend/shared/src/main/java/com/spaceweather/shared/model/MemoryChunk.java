package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MemoryChunk {
    private final String chunkId;
    private final String sourceEventId;
    private final ChunkType chunkType;
    private final String content;
    private double importance;
    private double feedbackScore;
    private final List<Double> embedding;
    private final Instant createdAt;

    @JsonCreator
    public MemoryChunk(
            @JsonProperty("chunkId") String chunkId,
            @JsonProperty("sourceEventId") String sourceEventId,
            @JsonProperty("chunkType") ChunkType chunkType,
            @JsonProperty("content") String content,
            @JsonProperty("importance") double importance,
            @JsonProperty("feedbackScore") double feedbackScore,
            @JsonProperty("embedding") List<Double> embedding,
            @JsonProperty("createdAt") Instant createdAt) {
        this.chunkId = Objects.requireNonNull(chunkId, "chunkId cannot be null");
        this.sourceEventId = sourceEventId;
        this.chunkType = Objects.requireNonNull(chunkType, "chunkType cannot be null");
        this.content = Objects.requireNonNull(content, "content cannot be null");
        this.importance = Math.max(0.0, Math.min(2.0, importance));
        this.feedbackScore = Math.max(0.0, Math.min(1.0, feedbackScore));
        this.embedding = embedding != null ? Collections.unmodifiableList(new ArrayList<>(embedding)) : Collections.emptyList();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getChunkId() { return chunkId; }
    public String getSourceEventId() { return sourceEventId; }
    public ChunkType getChunkType() { return chunkType; }
    public String getContent() { return content; }
    public double getImportance() { return importance; }
    public void setImportance(double importance) { this.importance = importance; }
    public double getFeedbackScore() { return feedbackScore; }
    public void setFeedbackScore(double feedbackScore) { this.feedbackScore = feedbackScore; }
    public List<Double> getEmbedding() { return embedding; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryChunk that = (MemoryChunk) o;
        return Objects.equals(chunkId, that.chunkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkId);
    }

    @Override
    public String toString() {
        return "MemoryChunk{" +
                "id='" + chunkId + '\'' +
                ", type=" + chunkType +
                ", feedback=" + feedbackScore +
                '}';
    }
}
