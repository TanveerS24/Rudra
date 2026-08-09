package com.spaceweather.gateway.config;

public class GatewayConfig {
    private final int port;
    private final String mainBackendUrl;
    private final String simulationServiceUrl;
    private final String wsTargetUrl;

    public GatewayConfig() {
        this.port = getEnvInt("GATEWAY_PORT", 8080);
        this.mainBackendUrl = getEnvString("MAIN_BACKEND_URL", "http://localhost:8081");
        this.simulationServiceUrl = getEnvString("SIMULATION_SERVICE_URL", "http://localhost:8082");
        this.wsTargetUrl = getEnvString("WS_TARGET_URL", "ws://localhost:8085");
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

    public int getPort() { return port; }
    public String getMainBackendUrl() { return mainBackendUrl; }
    public String getSimulationServiceUrl() { return simulationServiceUrl; }
    public String getWsTargetUrl() { return wsTargetUrl; }
}
