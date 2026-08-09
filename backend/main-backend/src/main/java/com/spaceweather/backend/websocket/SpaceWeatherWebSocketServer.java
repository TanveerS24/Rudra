package com.spaceweather.backend.websocket;

import com.spaceweather.shared.dto.WebSocketMessageDTO;
import com.spaceweather.shared.util.JsonUtils;
import com.spaceweather.shared.util.StructuredLogger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpaceWeatherWebSocketServer extends WebSocketServer {
    private static final StructuredLogger log = StructuredLogger.of(SpaceWeatherWebSocketServer.class, "MAIN-BACKEND");
    private final Set<WebSocket> clients = ConcurrentHashMap.newKeySet();

    public SpaceWeatherWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        log.info("New WebSocket client connected from: {}", conn.getRemoteSocketAddress());
        conn.send(JsonUtils.toJson(WebSocketMessageDTO.of("SYSTEM_CONNECTED", "Connected to Space Operations Live Telemetry Stream", null)));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        log.info("WebSocket client disconnected: {} (code={}, reason={})", conn.getRemoteSocketAddress(), code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        log.debug("Received WebSocket message: {}", message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.warn("WebSocket error on connection {}: {}", conn != null ? conn.getRemoteSocketAddress() : "global", ex.getMessage());
    }

    @Override
    public void onStart() {
        log.info("Space Weather WebSocket Server started on port: {}", getPort());
    }

    public void broadcastMessage(String type, Object payload, String correlationId) {
        if (clients.isEmpty()) return;
        WebSocketMessageDTO msg = WebSocketMessageDTO.of(type, payload, correlationId);
        String json = JsonUtils.toJson(msg);
        broadcast(json);
        log.debug("Broadcasted WebSocket event: {} to {} clients", type, clients.size());
    }

    public int getClientCount() {
        return clients.size();
    }
}
