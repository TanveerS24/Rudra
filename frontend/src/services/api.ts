import {
  DashboardSummary,
  SpaceWeatherEvent,
  Satellite,
  RiskAssessment,
  Recommendation,
  Alert,
  SimulationConfig,
  MemoryChunk,
  FeedbackScore,
} from '../types';

const API_BASE = '/api/v1';

async function fetchJson<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const requestId = 'REQ-' + Math.random().toString(36).substring(2, 10).toUpperCase();
  const headers = {
    'Content-Type': 'application/json',
    'X-Request-ID': requestId,
    ...(options?.headers || {}),
  };

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 6000);

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
      signal: options?.signal || controller.signal,
    });

    if (!response.ok) {
      let errorMsg = `API Error (${response.status}): ${response.statusText}`;
      try {
        const errJson = await response.json();
        if (errJson.message) errorMsg = errJson.message;
      } catch (_) {}
      throw new Error(errorMsg);
    }

    return await response.json();
  } finally {
    clearTimeout(timeoutId);
  }
}

export const apiService = {
  // Dashboard Telemetry
  getDashboardSummary: () => fetchJson<DashboardSummary>('/dashboard/summary'),

  // Space Weather Events
  getLatestEvent: () => fetchJson<SpaceWeatherEvent>('/events/latest'),
  getEventById: (id: string) => fetchJson<SpaceWeatherEvent>(`/events/${id}`),
  getRecentEvents: (limit = 20) => fetchJson<SpaceWeatherEvent[]>(`/events?limit=${limit}`),
  getHistoricalEvents: (limit = 50) => fetchJson<SpaceWeatherEvent[]>(`/history/events?limit=${limit}`),

  // Satellites
  getAllSatellites: () => fetchJson<Satellite[]>('/satellites'),
  getSatelliteById: (id: string) => fetchJson<Satellite>(`/satellites/${id}`),
  updateSatelliteStatus: (id: string, healthStatus?: string, operationalStatus?: string) =>
    fetchJson<{ updated: boolean }>(`/satellites/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ healthStatus, operationalStatus }),
    }),

  // Risk Assessments
  getRiskAssessments: (limit = 20) => fetchJson<RiskAssessment[]>(`/risk?limit=${limit}`),
  getRiskAssessmentsForEvent: (eventId: string) => fetchJson<RiskAssessment[]>(`/risk/event/${eventId}`),
  getHistoricalRisk: (limit = 50) => fetchJson<RiskAssessment[]>(`/history/risk?limit=${limit}`),

  // Recommendations
  getRecommendations: (limit = 20) => fetchJson<Recommendation[]>(`/recommendations?limit=${limit}`),
  updateRecommendationStatus: (id: string, status: 'EXECUTED' | 'DISMISSED') =>
    fetchJson<{ updated: boolean }>(`/recommendations/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    }),

  // Alerts
  getActiveAlerts: (limit = 20) => fetchJson<Alert[]>(`/alerts?limit=${limit}`),
  acknowledgeAlert: (id: string) =>
    fetchJson<{ acknowledged: boolean }>(`/alerts/${id}/acknowledge`, {
      method: 'PUT',
    }),

  // Simulator Controls
  getSimulationConfig: () => fetchJson<SimulationConfig>('/simulator/config'),
  updateSimulationConfig: (config: Partial<SimulationConfig>) =>
    fetchJson<SimulationConfig>('/simulator/config', {
      method: 'PUT',
      body: JSON.stringify(config),
    }),
  startSimulation: () => fetchJson<{ status: string }>('/simulator/start', { method: 'POST' }),
  pauseSimulation: () => fetchJson<{ status: string }>('/simulator/pause', { method: 'POST' }),
  resetSimulation: () => fetchJson<{ status: string }>('/simulator/reset', { method: 'POST' }),
  triggerGeneration: (intensity?: string, eventType?: string) =>
    fetchJson<SpaceWeatherEvent>('/simulator/generate', {
      method: 'POST',
      body: JSON.stringify({ intensity, eventType }),
    }),
  getSimulatorStatus: () => fetchJson<any>('/simulator/status'),
  getMemoryChunks: () => fetchJson<MemoryChunk[]>('/simulator/memories'),
  submitFeedback: (targetId: string, targetType: string, accuracyScore: number, usefulnessScore: number, comments: string) =>
    fetchJson<FeedbackScore>('/simulator/feedback', {
      method: 'POST',
      body: JSON.stringify({ targetId, targetType, accuracyScore, usefulnessScore, comments }),
    }),
};
