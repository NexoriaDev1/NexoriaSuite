package dev.nexoria.metrics;

public record MetricsSnapshot(
        double[] tps,
        double mspt,
        long usedMemoryMb,
        long maxMemoryMb,
        int activeThreads,
        long uptimeMillis
) {}
