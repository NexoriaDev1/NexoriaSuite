package dev.nexoria.metrics;

import org.bukkit.event.Listener;

public class MetricsListener implements Listener {

    private final NexoriaMetricsPlugin plugin;

    public MetricsListener(NexoriaMetricsPlugin plugin) {
        this.plugin = plugin;
    }
}
