import React, { useState, useEffect } from 'react';
import {
  Play,
  Pause,
  RotateCcw,
  Zap,
  Sliders,
  Database,
  Cpu,
  Layers,
  Star,
  CheckCircle,
  ArrowLeft,
} from 'lucide-react';
import { apiService } from '../services/api';
import { SimulationConfig, MemoryChunk, FeedbackScore } from '../types';

interface SimulatorProps {
  onBackToDashboard: () => void;
}

export const SimulatorPage: React.FC<SimulatorProps> = ({ onBackToDashboard }) => {
  const [config, setConfig] = useState<SimulationConfig>({
    intervalSeconds: 15,
    workerCount: 2,
    mode: 'HYBRID_LLM',
    defaultIntensity: 'MODERATE',
    isActive: true,
    updatedAt: new Date().toISOString(),
  });

  const [status, setStatus] = useState<any>(null);
  const [memories, setMemories] = useState<MemoryChunk[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);
  const [generateMsg, setGenerateMsg] = useState<string | null>(null);

  // Feedback Submission Form State
  const [feedbackTargetId, setFeedbackTargetId] = useState('EVT-HIST-2026-001');
  const [accuracyScore, setAccuracyScore] = useState(0.9);
  const [usefulnessScore, setUsefulnessScore] = useState(0.9);
  const [feedbackComments, setFeedbackComments] = useState('');
  const [feedbackSuccess, setFeedbackSuccess] = useState(false);

  // Load simulator configs and memories
  const refreshData = async () => {
    try {
      const [cfg, stat, mems] = await Promise.all([
        apiService.getSimulationConfig().catch(() => null),
        apiService.getSimulatorStatus().catch(() => null),
        apiService.getMemoryChunks().catch(() => []),
      ]);
      if (cfg) setConfig(cfg);
      if (stat) setStatus(stat);
      if (mems) setMemories(mems);
    } catch (err) {
      console.error('Failed to load simulator status:', err);
    }
  };

  useEffect(() => {
    refreshData();
    const interval = setInterval(refreshData, 3000);
    return () => clearInterval(interval);
  }, []);

  const handleStart = async () => {
    await apiService.startSimulation();
    setConfig((prev) => ({ ...prev, isActive: true }));
  };

  const handlePause = async () => {
    await apiService.pauseSimulation();
    setConfig((prev) => ({ ...prev, isActive: false }));
  };

  const handleReset = async () => {
    await apiService.resetSimulation();
    setConfig((prev) => ({ ...prev, isActive: false }));
  };

  const handleUpdateConfig = async (newCfg: Partial<SimulationConfig>) => {
    const updated = await apiService.updateSimulationConfig(newCfg);
    setConfig(updated);
  };

  const handleTriggerManual = async (intensity: string) => {
    setIsGenerating(true);
    setGenerateMsg('Generating space weather scenario through RAG + Llama pipeline...');
    try {
      const evt = await apiService.triggerGeneration(intensity);
      setGenerateMsg(`Generated ${evt.eventType} (${evt.intensity})! ID: ${evt.eventId}`);
      refreshData();
    } catch (err: any) {
      setGenerateMsg(`Generation error: ${err.message}`);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSubmitFeedback = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiService.submitFeedback(
        feedbackTargetId,
        'EVENT',
        accuracyScore,
        usefulnessScore,
        feedbackComments || 'Accurate scenario modeling and risk evaluation'
      );
      setFeedbackSuccess(true);
      setTimeout(() => setFeedbackSuccess(false), 3000);
      refreshData();
    } catch (err) {
      console.error('Failed to submit feedback:', err);
    }
  };

  return (
    <div className="h-screen w-screen flex flex-col bg-space-950 text-slate-100 scanline overflow-hidden font-sans select-none">
      {/* Header */}
      <header className="h-14 bg-space-900/90 border-b border-blue-900/40 px-6 flex items-center justify-between backdrop-blur-md shrink-0">
        <div className="flex items-center space-x-3">
          <button
            onClick={onBackToDashboard}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded bg-blue-950/60 hover:bg-blue-900/60 border border-hud-cyan/40 text-hud-cyan text-xs font-mono transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>RETURN TO MISSION CONTROL</span>
          </button>

          <div className="h-4 w-px bg-blue-800/60 mx-2" />

          <h1 className="text-sm font-bold font-mono text-slate-100 tracking-wider flex items-center space-x-2">
            <Sliders className="w-4 h-4 text-hud-cyan" />
            <span>SIMULATION SERVICE & RAG CONTROL DECK</span>
          </h1>
        </div>

        <div className="flex items-center space-x-2">
          <span className={`w-2.5 h-2.5 rounded-full ${config.isActive ? 'bg-emerald-400 animate-ping-slow' : 'bg-amber-400'}`} />
          <span className="text-xs font-mono font-bold text-slate-300">
            SCHEDULER: {config.isActive ? 'RUNNING' : 'PAUSED'}
          </span>
        </div>
      </header>

      {/* Main Simulator Deck Grid */}
      <div className="flex-1 grid grid-cols-12 gap-4 p-4 overflow-y-auto">
        {/* Left Column: Master Controls & Queue Telemetry (5 cols) */}
        <div className="col-span-12 lg:col-span-5 flex flex-col space-y-4">
          {/* Master Run Controls */}
          <div className="hud-panel p-5 rounded-lg space-y-4">
            <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
              <h2 className="text-xs font-bold font-mono text-hud-cyan uppercase tracking-wider">
                Simulation Scheduler Control
              </h2>
              <span className="text-[10px] font-mono text-slate-400">
                STATE: {config.isActive ? 'ACTIVE' : 'IDLE'}
              </span>
            </div>

            <div className="grid grid-cols-3 gap-3">
              <button
                onClick={handleStart}
                disabled={config.isActive}
                className="flex items-center justify-center space-x-2 py-2.5 rounded bg-emerald-600/30 hover:bg-emerald-600/50 disabled:opacity-50 border border-emerald-500/50 text-emerald-300 font-mono text-xs font-bold transition-all shadow-glow-emerald"
              >
                <Play className="w-4 h-4" />
                <span>START</span>
              </button>

              <button
                onClick={handlePause}
                disabled={!config.isActive}
                className="flex items-center justify-center space-x-2 py-2.5 rounded bg-amber-600/30 hover:bg-amber-600/50 disabled:opacity-50 border border-amber-500/50 text-amber-300 font-mono text-xs font-bold transition-all shadow-glow-amber"
              >
                <Pause className="w-4 h-4" />
                <span>PAUSE</span>
              </button>

              <button
                onClick={handleReset}
                className="flex items-center justify-center space-x-2 py-2.5 rounded bg-red-600/30 hover:bg-red-600/50 border border-red-500/50 text-red-300 font-mono text-xs font-bold transition-all"
              >
                <RotateCcw className="w-4 h-4" />
                <span>RESET</span>
              </button>
            </div>

            {/* Simulation Interval Slider */}
            <div className="space-y-2 pt-2">
              <div className="flex items-center justify-between text-xs font-mono">
                <span className="text-slate-300">TRIGGER INTERVAL:</span>
                <span className="text-hud-cyan font-bold">{config.intervalSeconds} SECONDS</span>
              </div>
              <input
                type="range"
                min={5}
                max={60}
                step={5}
                value={config.intervalSeconds}
                onChange={(e) => handleUpdateConfig({ intervalSeconds: Number(e.target.value) })}
                className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-hud-cyan"
              />
            </div>

            {/* Worker Count Slider */}
            <div className="space-y-2 pt-1">
              <div className="flex items-center justify-between text-xs font-mono">
                <span className="text-slate-300">PARALLEL WORKER THREADS:</span>
                <span className="text-hud-cyan font-bold">{config.workerCount} WORKERS</span>
              </div>
              <input
                type="range"
                min={1}
                max={8}
                step={1}
                value={config.workerCount}
                onChange={(e) => handleUpdateConfig({ workerCount: Number(e.target.value) })}
                className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-hud-cyan"
              />
            </div>

            {/* Generation Mode Selector */}
            <div className="space-y-2 pt-1">
              <span className="text-xs font-mono text-slate-300 block">GENERATION ENGINE:</span>
              <div className="grid grid-cols-2 gap-2 font-mono text-xs">
                {['HYBRID_LLM', 'DETERMINISTIC_ONLY'].map((mode) => (
                  <button
                    key={mode}
                    onClick={() => handleUpdateConfig({ mode })}
                    className={`py-2 px-3 rounded border text-left transition-colors ${config.mode === mode ? 'bg-hud-cyan/20 border-hud-cyan text-hud-cyan font-bold' : 'bg-space-950/50 border-slate-800 text-slate-400'}`}
                  >
                    {mode === 'HYBRID_LLM' ? 'LLM + RAG MEMORY' : 'DETERMINISTIC PHYSICS'}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Queue Telemetry & One-Off Generator */}
          <div className="hud-panel p-5 rounded-lg space-y-4">
            <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
              <h2 className="text-xs font-bold font-mono text-hud-cyan uppercase tracking-wider">
                Worker Queue Telemetry
              </h2>
              <Layers className="w-4 h-4 text-hud-cyan" />
            </div>

            <div className="grid grid-cols-3 gap-2 font-mono text-center">
              <div className="p-2.5 rounded bg-space-900/90 border border-blue-900/50">
                <div className="text-[10px] text-slate-400">QUEUE SIZE</div>
                <div className="text-lg font-bold text-hud-cyan">{status?.queueSize ?? 0}</div>
              </div>
              <div className="p-2.5 rounded bg-space-900/90 border border-blue-900/50">
                <div className="text-[10px] text-slate-400">PROCESSED</div>
                <div className="text-lg font-bold text-emerald-400">{status?.processedCount ?? 0}</div>
              </div>
              <div className="p-2.5 rounded bg-space-900/90 border border-blue-900/50">
                <div className="text-[10px] text-slate-400">FAILED</div>
                <div className="text-lg font-bold text-red-400">{status?.failedCount ?? 0}</div>
              </div>
            </div>

            {/* One-off Scenario Generator */}
            <div className="pt-2 space-y-2">
              <span className="text-xs font-mono text-slate-300 block">INSTANT SCENARIO INJECTION:</span>
              <div className="grid grid-cols-3 gap-2 font-mono text-xs">
                {['CRITICAL', 'HIGH', 'MODERATE'].map((lvl) => (
                  <button
                    key={lvl}
                    disabled={isGenerating}
                    onClick={() => handleTriggerManual(lvl)}
                    className="py-2 px-2 rounded bg-space-900/90 hover:bg-blue-950/80 border border-blue-800 text-hud-cyan text-center font-bold transition-all disabled:opacity-50"
                  >
                    TRIGGER {lvl}
                  </button>
                ))}
              </div>

              {generateMsg && (
                <div className="p-2 rounded bg-space-950/80 border border-hud-cyan/40 text-[11px] font-mono text-hud-cyan">
                  {generateMsg}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: RAG Memory Inspector & Feedback Scoring (7 cols) */}
        <div className="col-span-12 lg:col-span-7 flex flex-col space-y-4">
          {/* RAG Semantic Memory Inspector */}
          <div className="hud-panel p-5 rounded-lg space-y-3 flex-1 flex flex-col">
            <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
              <div className="flex items-center space-x-2 text-hud-purple">
                <Database className="w-4 h-4" />
                <h2 className="text-xs font-bold font-mono tracking-wider uppercase">
                  Persistent Semantic RAG Memories ({memories.length})
                </h2>
              </div>
              <span className="text-[10px] font-mono text-slate-400">
                COSINE SIMILARITY + WEIGHTED FEEDBACK
              </span>
            </div>

            {/* Memories List */}
            <div className="space-y-2 overflow-y-auto max-h-[300px] pr-1">
              {memories.map((m) => (
                <div
                  key={m.chunkId}
                  className="p-3 rounded bg-space-900/80 border border-blue-900/40 text-xs space-y-1.5"
                >
                  <div className="flex items-center justify-between font-mono text-[10px]">
                    <span className="px-1.5 py-0.5 rounded bg-purple-950/60 text-purple-300 border border-purple-800/50 font-bold">
                      {m.chunkType}
                    </span>
                    <span className="text-slate-400">
                      FEEDBACK WEIGHT: <strong className="text-hud-amber">{(m.feedbackScore * 100).toFixed(0)}%</strong> | IMPORTANCE: {m.importance}
                    </span>
                  </div>

                  <p className="text-slate-200 font-sans leading-relaxed">{m.content}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Feedback Scoring Submission Panel */}
          <div className="hud-panel p-5 rounded-lg space-y-3">
            <div className="flex items-center justify-between border-b border-blue-900/40 pb-2">
              <div className="flex items-center space-x-2 text-hud-amber">
                <Star className="w-4 h-4" />
                <h2 className="text-xs font-bold font-mono tracking-wider uppercase">
                  Feedback Scoring & Memory Reinforcement
                </h2>
              </div>
              <span className="text-[10px] font-mono text-slate-400">
                INFLUENCES FUTURE RETRIEVAL
              </span>
            </div>

            <form onSubmit={handleSubmitFeedback} className="space-y-3 text-xs font-mono">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-slate-400 block mb-1">TARGET EVENT / ID:</label>
                  <input
                    type="text"
                    value={feedbackTargetId}
                    onChange={(e) => setFeedbackTargetId(e.target.value)}
                    className="w-full bg-space-950 border border-slate-700 rounded px-2.5 py-1.5 text-slate-200"
                  />
                </div>

                <div>
                  <label className="text-slate-400 block mb-1">ACCURACY RATING (0.0 - 1.0):</label>
                  <input
                    type="number"
                    step="0.05"
                    min="0"
                    max="1"
                    value={accuracyScore}
                    onChange={(e) => setAccuracyScore(Number(e.target.value))}
                    className="w-full bg-space-950 border border-slate-700 rounded px-2.5 py-1.5 text-slate-200"
                  />
                </div>
              </div>

              <div>
                <label className="text-slate-400 block mb-1">OPERATIONAL COMMENTS:</label>
                <input
                  type="text"
                  placeholder="e.g., Highly accurate drag prediction on LEO constellations"
                  value={feedbackComments}
                  onChange={(e) => setFeedbackComments(e.target.value)}
                  className="w-full bg-space-950 border border-slate-700 rounded px-2.5 py-1.5 text-slate-200 font-sans"
                />
              </div>

              <div className="flex items-center justify-between pt-1">
                {feedbackSuccess && (
                  <span className="text-emerald-400 text-xs flex items-center space-x-1">
                    <CheckCircle className="w-3.5 h-3.5" />
                    <span>Memory weighted successfully!</span>
                  </span>
                )}
                <button
                  type="submit"
                  className="ml-auto px-4 py-2 rounded bg-hud-amber/20 hover:bg-hud-amber/40 border border-hud-amber text-hud-amber font-bold transition-all shadow-glow-amber"
                >
                  SUBMIT QUALITY FEEDBACK
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};
