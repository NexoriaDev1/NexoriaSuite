package dev.nexoria.announcer;

import java.util.List;

/**
 * Public developer API for NexoriaAnnouncer.
 */
public class AnnouncerAPI {

    private final NexoriaAnnouncerPlugin plugin;

    public AnnouncerAPI(NexoriaAnnouncerPlugin plugin) {
        this.plugin = plugin;
    }

    public void broadcast(String message) {
        plugin.getAnnouncerManager().manualBroadcast(message);
    }

    public void triggerNext() {
        plugin.getAnnouncerManager().broadcastNext();
    }

    public List<Announcement> getAnnouncements() {
        return plugin.getAnnouncerManager().getAnnouncements();
    }

    public AnnouncerManager getManager() {
        return plugin.getAnnouncerManager();
    }
}
