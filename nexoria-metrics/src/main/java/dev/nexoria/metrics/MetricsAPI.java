package dev.nexoria.metrics;

/**
 * Public developer API for NexoriaMetrics.
 */
public class MetricsAPI {

    private final NexoriaMetricsPlugin plugin;

    public MetricsAPI(NexoriaMetricsPlugin plugin) {
        this.plugin = plugin;
    }

    public MetricsSnapshot getSnapshot() {
        return plugin.getMetricsManager().getSnapshot();
    }

    public MetricsManager getManager() {
        return plugin.getMetricsManager();
    }
}
