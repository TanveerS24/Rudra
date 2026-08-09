export type EventType =
  | 'SOLAR_FLARE'
  | 'CORONAL_MASS_EJECTION'
  | 'GEOMAGNETIC_STORM'
  | 'SOLAR_RADIATION_STORM'
  | 'RADIO_BLACKOUT'
  | 'COMBINED_EVENT';

export type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';
export type OrbitType = 'LEO' | 'MEO' | 'GEO' | 'HEO';
export type HealthStatus = 'NOMINAL' | 'DEGRADED' | 'CRITICAL' | 'SAFE_MODE' | 'OFFLINE';
export type SensitivityLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type OperationalStatus = 'ACTIVE' | 'STANDBY' | 'SAFE_MODE' | 'DECOMMISSIONED';

export interface LocationCoordinates {
  latitude: number;
  longitude: number;
}

export interface SpaceWeatherEvent {
  eventId: string;
  timestamp: string;
  eventType: EventType;
  intensity: string;
  durationMinutes: number;
  solarWindSpeed: number;
  geomagneticIndex: number;
  radiationLevel: string;
  origin: LocationCoordinates;
  affectedRegions: string[];
  maximumImpactLocation: LocationCoordinates;
  impactDescription: string;
  confidence: number;
}

export interface Satellite {
  satelliteId: string;
  name: string;
  missionType: string;
  orbitType: OrbitType;
  altitudeKm: number;
  inclinationDeg: number;
  latitude: number;
  longitude: number;
  healthStatus: HealthStatus;
  radiationSensitivity: SensitivityLevel;
  communicationSensitivity: SensitivityLevel;
  navigationSensitivity: SensitivityLevel;
  operationalStatus: OperationalStatus;
  createdAt: string;
  updatedAt: string;
}

export interface RiskAssessment {
  assessmentId: string;
  eventId: string;
  satelliteId: string;
  satelliteName?: string;
  deterministicScore: number;
  finalScore: number;
  riskLevel: RiskLevel;
  primaryFactors: string[];
  potentialEffects: string[];
  createdAt: string;
}

export interface Recommendation {
  recommendationId: string;
  eventId: string;
  assessmentId?: string;
  satelliteId?: string;
  action: string;
  reasoning: string;
  expectedImpact: string;
  confidence: number;
  status: 'PENDING' | 'EXECUTED' | 'DISMISSED';
  createdAt: string;
}

export interface Alert {
  alertId: string;
  eventId?: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  title: string;
  message: string;
  acknowledged: boolean;
  createdAt: string;
}

export interface SimulationConfig {
  intervalSeconds: number;
  workerCount: number;
  mode: string;
  defaultIntensity: string;
  isActive: boolean;
  updatedAt: string;
}

export interface MemoryChunk {
  chunkId: string;
  sourceEventId: string;
  chunkType: string;
  content: string;
  importance: number;
  feedbackScore: number;
  embedding: number[];
  createdAt: string;
}

export interface FeedbackScore {
  scoreId: string;
  targetId: string;
  targetType: string;
  accuracyScore: number;
  usefulnessScore: number;
  compositeScore: number;
  comments: string;
  createdAt: string;
}

export interface DashboardSummary {
  systemStatus: string;
  timestamp: string;
  latestEvent: SpaceWeatherEvent | null;
  highestRiskAssessment: RiskAssessment | null;
  activeRiskAssessments: RiskAssessment[];
  satellites: Satellite[];
  pendingRecommendations: Recommendation[];
  activeAlerts: Alert[];
  simulationConfig: SimulationConfig;
  environmentMetrics: {
    solarWindSpeed: number;
    geomagneticIndex: number;
    radiationLevel: string;
    intensity: string;
    eventType: string;
    durationMinutes: number;
  };
  systemHealth: {
    mainBackend: string;
    database: string;
    websocket: string;
    ollamaLLM: string;
  };
}

export interface WebSocketMessage {
  type: string;
  timestamp: string;
  payload: any;
  correlationId?: string;
}
