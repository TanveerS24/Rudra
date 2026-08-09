package com.spaceweather.gateway;

import com.spaceweather.gateway.config.GatewayConfig;
import com.spaceweather.gateway.routing.HealthCheckHandler;
import com.spaceweather.gateway.routing.ProxyHandler;
import com.spaceweather.shared.util.StructuredLogger;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ApiGatewayApplication {
    private static final StructuredLogger log = StructuredLogger.of(ApiGatewayApplication.class, "API-GATEWAY");

    public static void main(String[] args) {
        try {
            GatewayConfig config = new GatewayConfig();
            log.info("Starting API Gateway on port {}...", config.getPort());

            HttpServer server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            ProxyHandler proxyHandler = new ProxyHandler(config);
            HealthCheckHandler healthHandler = new HealthCheckHandler(config);

            // API Routes
            server.createContext("/api", proxyHandler);
            server.createContext("/health", healthHandler);
            server.createContext("/ready", healthHandler);

            server.start();
            log.info("API Gateway successfully started and listening on http://0.0.0.0:{}", config.getPort());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down API Gateway...");
                server.stop(1);
                log.info("API Gateway stopped.");
            }));

        } catch (Exception e) {
            log.error("Fatal error starting API Gateway", e);
            System.exit(1);
        }
    }
}
