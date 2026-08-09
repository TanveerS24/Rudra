package com.spaceweather.simulation.queue;

import com.spaceweather.shared.util.StructuredLogger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class SimulationQueue implements AutoCloseable {
    private static final StructuredLogger log = StructuredLogger.of(SimulationQueue.class, "SIMULATION-SERVICE");
    private final BlockingQueue<SimulationTask> queue;
    private final ExecutorService workerPool;
    private final int workerCount;
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong failedCount = new AtomicLong(0);
    private volatile boolean running = true;

    public SimulationQueue(int workerCount, Consumer<SimulationTask> taskProcessor) {
        this.workerCount = Math.max(1, workerCount);
        this.queue = new LinkedBlockingQueue<>(500);
        this.workerPool = Executors.newFixedThreadPool(this.workerCount);

        for (int i = 0; i < this.workerCount; i++) {
            final int workerId = i + 1;
            workerPool.submit(() -> {
                log.info("Simulation worker #{} started.", workerId);
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        SimulationTask task = queue.poll(1, TimeUnit.SECONDS);
                        if (task != null) {
                            try {
                                taskProcessor.accept(task);
                                processedCount.incrementAndGet();
                            } catch (Exception e) {
                                failedCount.incrementAndGet();
                                task.getFuture().completeExceptionally(e);
                                log.error("Worker #" + workerId + " error executing task " + task.getTaskId(), e);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                log.info("Simulation worker #{} stopped.", workerId);
            });
        }
    }

    public boolean enqueue(SimulationTask task) {
        if (!running) return false;
        boolean accepted = queue.offer(task);
        if (!accepted) {
            log.warn("Simulation queue is full. Rejecting task {}.", task.getTaskId());
            task.getFuture().completeExceptionally(new RejectedExecutionException("Simulation queue is full"));
        }
        return accepted;
    }

    public int getQueueSize() { return queue.size(); }
    public long getProcessedCount() { return processedCount.get(); }
    public long getFailedCount() { return failedCount.get(); }
    public int getWorkerCount() { return workerCount; }

    @Override
    public void close() {
        running = false;
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(3, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
        }
    }
}
