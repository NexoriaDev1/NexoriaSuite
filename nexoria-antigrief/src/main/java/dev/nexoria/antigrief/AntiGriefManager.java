package dev.nexoria.antigrief;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AntiGriefManager {

    private final NexoriaAntiGriefPlugin plugin;
    private final Map<String, Boolean> protections = new ConcurrentHashMap<>();

    public AntiGriefManager(NexoriaAntiGriefPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        protections.clear();
        for (String key : plugin.getConfig().getConfigurationSection("protections").getKeys(false)) {
            protections.put(key, plugin.getConfig().getBoolean("protections." + key, false));
        }
    }

    public boolean isProtected(String feature) {
        return protections.getOrDefault(feature, false);
    }

    public boolean toggleProtection(String feature) {
        boolean current = isProtected(feature);
        boolean next = !current;
        protections.put(feature, next);
        plugin.getConfig().set("protections." + feature, next);
        plugin.saveConfig();
        return next;
    }

    public Map<String, Boolean> getProtections() {
        return Collections.unmodifiableMap(protections);
    }
}
