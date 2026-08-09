package com.spaceweather.backend.config;

public class AppConfig {
    private final int port;
    private final int wsPort;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final int dbMaxPoolSize;
    private final String ollamaUrl;
    private final String ollamaModel;
    private final boolean deterministicFallback;
    private final String simulationServiceUrl;

    public AppConfig() {
        this.port = getEnvInt("PORT", 8081);
        this.wsPort = getEnvInt("WS_PORT", 8085);
        this.dbUrl = getEnvString("DB_URL", "jdbc:mysql://localhost:3306/spaceweather_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        this.dbUser = getEnvString("DB_USER", "root");
        this.dbPassword = getEnvString("DB_PASSWORD", "root");
        this.dbMaxPoolSize = getEnvInt("DB_MAX_POOL_SIZE", 10);
        this.ollamaUrl = getEnvString("OLLAMA_URL", "http://localhost:11434");
        this.ollamaModel = getEnvString("OLLAMA_MODEL", "llama3.1:8b");
        this.deterministicFallback = getEnvBool("DETERMINISTIC_FALLBACK", true);
        this.simulationServiceUrl = getEnvString("SIMULATION_SERVICE_URL", "http://localhost:8082");
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
    public int getWsPort() { return wsPort; }
    public String getDbUrl() { return dbUrl; }
    public String getDbUser() { return dbUser; }
    public String getDbPassword() { return dbPassword; }
    public int getDbMaxPoolSize() { return dbMaxPoolSize; }
    public String getOllamaUrl() { return ollamaUrl; }
    public String getOllamaModel() { return ollamaModel; }
    public boolean isDeterministicFallback() { return deterministicFallback; }
    public String getSimulationServiceUrl() { return simulationServiceUrl; }
}
