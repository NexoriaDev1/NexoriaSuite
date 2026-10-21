package dev.nexoria.antigrief;

import java.util.Map;

/**
 * Public developer API for NexoriaAntiGrief.
 */
public class AntiGriefAPI {

    private final NexoriaAntiGriefPlugin plugin;

    public AntiGriefAPI(NexoriaAntiGriefPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isProtected(String feature) {
        return plugin.getAntiGriefManager().isProtected(feature);
    }

    public boolean toggleProtection(String feature) {
        return plugin.getAntiGriefManager().toggleProtection(feature);
    }

    public Map<String, Boolean> getProtections() {
        return plugin.getAntiGriefManager().getProtections();
    }

    public AntiGriefManager getManager() {
        return plugin.getAntiGriefManager();
    }
}
