package dev.nexoria.holograms;

import org.bukkit.Location;

import java.util.Map;

/**
 * Public developer API for NexoriaHolograms.
 */
public class HologramsAPI {

    private final NexoriaHologramsPlugin plugin;

    public HologramsAPI(NexoriaHologramsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean createHologram(String name, Location location) {
        return plugin.getHologramManager().createHologram(name, location);
    }

    public boolean deleteHologram(String name) {
        return plugin.getHologramManager().deleteHologram(name);
    }

    public boolean addLine(String name, String text) {
        return plugin.getHologramManager().addLine(name, text);
    }

    public Hologram getHologram(String name) {
        return plugin.getHologramManager().getHologram(name);
    }

    public Map<String, Hologram> getHolograms() {
        return plugin.getHologramManager().getHolograms();
    }

    public HologramManager getManager() {
        return plugin.getHologramManager();
    }
}
