package com.spaceweather.simulation.memory;

import com.spaceweather.simulation.config.SimulationServiceConfig;
import com.spaceweather.shared.util.JsonUtils;
import com.spaceweather.shared.util.StructuredLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;

public class EmbeddingService {
    private static final StructuredLogger log = StructuredLogger.of(EmbeddingService.class, "SIMULATION-SERVICE");
    private static final int VECTOR_DIMENSIONS = 64;
    private final SimulationServiceConfig config;
    private final HttpClient httpClient;

    public EmbeddingService(SimulationServiceConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    public List<Double> computeEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return Collections.nCopies(VECTOR_DIMENSIONS, 0.0);
        }

        // Try Ollama embeddings if configured
        if (!config.isFallbackSimulation()) {
            try {
                List<Double> ollamaVec = fetchOllamaEmbedding(text);
                if (ollamaVec != null && !ollamaVec.isEmpty()) {
                    return ollamaVec;
                }
            } catch (Exception e) {
                log.debug("Ollama embedding fetch failed: {}. Using deterministic vectorizer.", e.getMessage());
            }
        }

        return computeDeterministicEmbedding(text);
    }

    private List<Double> fetchOllamaEmbedding(String text) throws Exception {
        Map<String, Object> body = Map.of(
                "model", config.getOllamaModel(),
                "prompt", text
        );
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(config.getOllamaUrl() + "/api/embeddings"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(3))
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(body)))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            Map<?, ?> json = JsonUtils.fromJson(resp.body(), Map.class);
            List<?> raw = (List<?>) json.get("embedding");
            if (raw != null) {
                List<Double> result = new ArrayList<>(raw.size());
                for (Object o : raw) {
                    if (o instanceof Number n) result.add(n.doubleValue());
                }
                return normalize(result);
            }
        }
        return null;
    }

    public List<Double> computeDeterministicEmbedding(String text) {
        double[] vec = new double[VECTOR_DIMENSIONS];
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9 ]", " ");
        String[] words = cleaned.split("\\s+");

        for (String word : words) {
            if (word.isBlank()) continue;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hash = md.digest(word.getBytes(StandardCharsets.UTF_8));
                for (int i = 0; i < hash.length && i < VECTOR_DIMENSIONS; i++) {
                    int idx = Math.abs(hash[i] % VECTOR_DIMENSIONS);
                    vec[idx] += 1.0;
                }
                // Also add character n-grams
                for (int i = 0; i < word.length() - 2; i++) {
                    String trigram = word.substring(i, i + 3);
                    int hashTrigram = Math.abs(trigram.hashCode() % VECTOR_DIMENSIONS);
                    vec[hashTrigram] += 0.5;
                }
            } catch (Exception ignored) {}
        }

        List<Double> list = new ArrayList<>(VECTOR_DIMENSIONS);
        for (double v : vec) list.add(v);
        return normalize(list);
    }

    public static double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1 == null || v2 == null || v1.isEmpty() || v2.isEmpty()) return 0.0;
        int len = Math.min(v1.size(), v2.size());
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < len; i++) {
            double a = v1.get(i);
            double b = v2.get(i);
            dot += a * b;
            normA += a * a;
            normB += b * b;
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return Math.max(0.0, dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    private static List<Double> normalize(List<Double> vec) {
        double sumSq = 0.0;
        for (double d : vec) sumSq += d * d;
        if (sumSq == 0.0) return vec;
        double norm = Math.sqrt(sumSq);
        List<Double> normalized = new ArrayList<>(vec.size());
        for (double d : vec) normalized.add(d / norm);
        return normalized;
    }
}
