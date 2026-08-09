import { WebSocketMessage } from '../types';

type MessageHandler = (msg: WebSocketMessage) => void;

class WebSocketClient {
  private ws: WebSocket | null = null;
  private listeners: Set<MessageHandler> = new Set();
  private isConnected = false;
  private reconnectAttempts = 0;
  private maxReconnectDelay = 10000;
  private reconnectTimer: number | null = null;
  private url: string;

  constructor() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    // Port 8085 is the pure Java WebSocket Server on Main Backend
    this.url = `${protocol}//${window.location.hostname}:8085`;
  }

  public connect(): void {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return;
    }

    try {
      this.ws = new WebSocket(this.url);

      this.ws.onopen = () => {
        this.isConnected = true;
        this.reconnectAttempts = 0;
        console.log('[MISSION CONTROL WS] Connected to live space telemetry stream.');
      };

      this.ws.onmessage = (event) => {
        try {
          const parsed: WebSocketMessage = JSON.parse(event.data);
          this.listeners.forEach((listener) => {
            try {
              listener(parsed);
            } catch (err) {
              console.error('[WS Listener Error]', err);
            }
          });
        } catch (e) {
          console.debug('[WS Raw Message]', event.data);
        }
      };

      this.ws.onclose = () => {
        this.isConnected = false;
        this.scheduleReconnect();
      };

      this.ws.onerror = (err) => {
        console.warn('[MISSION CONTROL WS] WebSocket encountered an error, scheduling reconnect.', err);
        this.ws?.close();
      };
    } catch (e) {
      console.warn('[MISSION CONTROL WS] Connection initialization failed:', e);
      this.scheduleReconnect();
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer !== null) return;

    const delay = Math.min(1000 * Math.pow(1.5, this.reconnectAttempts), this.maxReconnectDelay);
    this.reconnectAttempts++;
    console.log(`[MISSION CONTROL WS] Reconnecting in ${Math.round(delay)}ms (attempt ${this.reconnectAttempts})...`);

    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null;
      this.connect();
    }, delay);
  }

  public subscribe(handler: MessageHandler): () => void {
    this.listeners.add(handler);
    return () => {
      this.listeners.delete(handler);
    };
  }

  public getStatus(): boolean {
    return this.isConnected;
  }

  public disconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}

export const wsClient = new WebSocketClient();
