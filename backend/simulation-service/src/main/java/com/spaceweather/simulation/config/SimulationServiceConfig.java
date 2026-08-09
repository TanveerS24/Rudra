package com.spaceweather.simulation.config;

public class SimulationServiceConfig {
    private final int port;
    private final String mainBackendUrl;
    private final String ollamaUrl;
    private final String ollamaModel;
    private final int workerCount;
    private final int intervalSeconds;
    private final boolean fallbackSimulation;
    private final String vectorStorePath;

    public SimulationServiceConfig() {
        this.port = getEnvInt("SIMULATION_PORT", 8082);
        this.mainBackendUrl = getEnvString("MAIN_BACKEND_URL", "http://localhost:8081");
        this.ollamaUrl = getEnvString("OLLAMA_URL", "http://localhost:11434");
        this.ollamaModel = getEnvString("OLLAMA_MODEL", "llama3.1:8b");
        this.workerCount = getEnvInt("SIMULATION_WORKER_COUNT", 2);
        this.intervalSeconds = getEnvInt("SIMULATION_INTERVAL", 15);
        this.fallbackSimulation = getEnvBool("SIMULATION_FALLBACK", true);
        this.vectorStorePath = getEnvString("VECTOR_STORE_PATH", "./data/memory");
    }

    private static String getEnvString(String key, String defaultValue) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    private static int getEnvInt(String key, int defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean getEnvBool(String key, boolean defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    public int getPort() { return port; }
    public String getMainBackendUrl() { return mainBackendUrl; }
    public String getOllamaUrl() { return ollamaUrl; }
    public String getOllamaModel() { return ollamaModel; }
    public int getWorkerCount() { return workerCount; }
    public int getIntervalSeconds() { return intervalSeconds; }
    public boolean isFallbackSimulation() { return fallbackSimulation; }
    public String getVectorStorePath() { return vectorStorePath; }
}
