package dev.nexoria.rtp;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Developer API contract for NexoriaRTP.
 */
public class RTPAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, int activeSessions) {}

    private final NexoriaRTPPlugin plugin;
    private final Logger logger;
    private final Map<UUID, Long> sessionRegistry = new ConcurrentHashMap<>();
    private volatile boolean active;

    public RTPAPI(NexoriaRTPPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public ServiceHealth getStatus() {
        return new ServiceHealth("NexoriaRTP", active, Instant.now(), sessionRegistry.size());
    }

    public CompletableFuture<Void> supplyAsync(Runnable task) {
        return CompletableFuture.runAsync(task);
    }

    public void registerSession(UUID uuid) {
        sessionRegistry.put(uuid, System.currentTimeMillis());
    }

    public void invalidateSession(UUID uuid) {
        sessionRegistry.remove(uuid);
    }

    public Map<UUID, Long> getActiveSessions() {
        return Collections.unmodifiableMap(sessionRegistry);
    }

    protected void shutdown() {
        this.active = false;
        this.sessionRegistry.clear();
    }

    public NexoriaRTPPlugin getPlugin() {
        return plugin;
    }
}
