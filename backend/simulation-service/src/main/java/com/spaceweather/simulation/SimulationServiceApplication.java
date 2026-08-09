package com.spaceweather.simulation;

import com.spaceweather.simulation.api.SimulatorController;
import com.spaceweather.simulation.application.SimulationEngine;
import com.spaceweather.simulation.chunking.SemanticChunker;
import com.spaceweather.simulation.chunking.SpaceWeatherSemanticChunker;
import com.spaceweather.simulation.config.SimulationServiceConfig;
import com.spaceweather.simulation.feedback.FeedbackService;
import com.spaceweather.simulation.llm.LLMClient;
import com.spaceweather.simulation.llm.OllamaLLMClient;
import com.spaceweather.simulation.memory.EmbeddingService;
import com.spaceweather.simulation.memory.MemoryStore;
import com.spaceweather.simulation.memory.VectorMemoryStore;
import com.spaceweather.shared.util.StructuredLogger;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class SimulationServiceApplication {
    private static final StructuredLogger log = StructuredLogger.of(SimulationServiceApplication.class, "SIMULATION-SERVICE");

    public static void main(String[] args) {
        try {
            SimulationServiceConfig config = new SimulationServiceConfig();
            log.info("Starting Space Weather Simulation Service on port {}...", config.getPort());

            // 1. Memory and RAG Infrastructure
            EmbeddingService embeddingService = new EmbeddingService(config);
            MemoryStore memoryStore = new VectorMemoryStore(embeddingService);
            SemanticChunker semanticChunker = new SpaceWeatherSemanticChunker(embeddingService);
            FeedbackService feedbackService = new FeedbackService(memoryStore, semanticChunker);

            // 2. LLM Client
            LLMClient llmClient = new OllamaLLMClient(config);

            // 3. Simulation Engine
            SimulationEngine engine = new SimulationEngine(config, llmClient, memoryStore, semanticChunker);

            // 4. Pure Java HTTP Server
            HttpServer server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            SimulatorController controller = new SimulatorController(engine, memoryStore, feedbackService);
            server.createContext("/api/v1/simulator", controller);
            server.createContext("/health", controller);
            server.createContext("/ready", controller);

            server.start();
            log.info("Space Weather Simulation Service successfully started and listening on http://0.0.0.0:{}", config.getPort());

            // Graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down Simulation Service...");
                server.stop(1);
                engine.close();
                log.info("Simulation Service stopped.");
            }));

        } catch (Exception e) {
            log.error("Fatal error starting Simulation Service", e);
            System.exit(1);
        }
    }
}
