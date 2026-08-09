package com.spaceweather.backend;

import com.spaceweather.backend.api.*;
import com.spaceweather.backend.application.DashboardService;
import com.spaceweather.backend.application.SatelliteService;
import com.spaceweather.backend.application.SpaceWeatherService;
import com.spaceweather.backend.config.AppConfig;
import com.spaceweather.backend.config.DatabaseConnectionPool;
import com.spaceweather.backend.decision.DecisionEngine;
import com.spaceweather.backend.persistence.*;
import com.spaceweather.backend.risk.DeterministicRiskEngine;
import com.spaceweather.backend.risk.HybridRiskAggregator;
import com.spaceweather.backend.risk.LLMRiskExplainer;
import com.spaceweather.backend.risk.RiskPolicy;
import com.spaceweather.backend.websocket.SpaceWeatherWebSocketServer;
import com.spaceweather.shared.util.StructuredLogger;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class MainBackendApplication {
    private static final StructuredLogger log = StructuredLogger.of(MainBackendApplication.class, "MAIN-BACKEND");

    public static void main(String[] args) {
        try {
            AppConfig config = new AppConfig();
            log.info("Starting Main Backend Service on HTTP port {} and WebSocket port {}...", config.getPort(), config.getWsPort());

            // 1. Database Connection Pool
            DatabaseConnectionPool dbPool = new DatabaseConnectionPool(config);

            // 2. Repositories
            EventRepository eventRepo = new MySqlEventRepository(dbPool);
            SatelliteRepository satRepo = new MySqlSatelliteRepository(dbPool);
            RiskAssessmentRepository riskRepo = new MySqlRiskAssessmentRepository(dbPool);
            RecommendationRepository recRepo = new MySqlRecommendationRepository(dbPool);
            AlertRepository alertRepo = new MySqlAlertRepository(dbPool);
            MissionRepository missionRepo = new MySqlMissionRepository(dbPool);

            // 3. WebSocket Server
            SpaceWeatherWebSocketServer wsServer = new SpaceWeatherWebSocketServer(config.getWsPort());
            wsServer.start();
            log.info("WebSocket Server started on port: {}", config.getWsPort());

            // 4. Hybrid Risk & Decision Engines
            RiskPolicy riskPolicy = new RiskPolicy();
            DeterministicRiskEngine deterministicEngine = new DeterministicRiskEngine(riskPolicy);
            LLMRiskExplainer llmExplainer = new LLMRiskExplainer(config);
            HybridRiskAggregator riskAggregator = new HybridRiskAggregator(deterministicEngine, llmExplainer);
            DecisionEngine decisionEngine = new DecisionEngine();

            // 5. Application Services
            SpaceWeatherService spaceWeatherService = new SpaceWeatherService(
                    eventRepo, satRepo, riskRepo, recRepo, alertRepo, riskAggregator, decisionEngine, wsServer
            );
            SatelliteService satelliteService = new SatelliteService(satRepo, wsServer);
            DashboardService dashboardService = new DashboardService(
                    spaceWeatherService, satelliteService, riskRepo, recRepo, alertRepo, dbPool, config
            );

            // 6. Pure Java HTTP Server
            HttpServer server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            server.createContext("/api/v1/events", new EventController(spaceWeatherService, satRepo));
            server.createContext("/api/v1/satellites", new SatelliteController(satelliteService));
            server.createContext("/api/v1/risk", new RiskController(riskRepo, satRepo, spaceWeatherService));
            server.createContext("/api/v1/recommendations", new RecommendationController(recRepo, spaceWeatherService));
            server.createContext("/api/v1/alerts", new AlertController(alertRepo, spaceWeatherService));
            server.createContext("/api/v1/dashboard/summary", new DashboardController(dashboardService));
            server.createContext("/api/v1/history", new HistoryController(spaceWeatherService, riskRepo, satRepo));
            server.createContext("/health", new HealthController(dbPool));
            server.createContext("/ready", new HealthController(dbPool));

            server.start();
            log.info("Main Backend Service successfully started and listening on http://0.0.0.0:{}", config.getPort());

            // Graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down Main Backend Service...");
                server.stop(1);
                try {
                    wsServer.stop();
                } catch (Exception ignored) {}
                dbPool.close();
                log.info("Main Backend Service stopped.");
            }));

        } catch (Exception e) {
            log.error("Fatal error starting Main Backend Service", e);
            System.exit(1);
        }
    }
}
