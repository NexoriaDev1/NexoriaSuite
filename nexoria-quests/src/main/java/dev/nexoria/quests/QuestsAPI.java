package dev.nexoria.quests;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public final class QuestsAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, int totalQuests) {}

    private final NexoriaQuestsPlugin plugin;

    public QuestsAPI(NexoriaQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return plugin.isEnabled();
    }

    public ServiceHealth getStatus() {
        int count = plugin.getQuestManager() != null ? plugin.getQuestManager().getAllQuests().size() : 0;
        return new ServiceHealth("NexoriaQuests", isActive(), Instant.now(), count);
    }

    public Collection<Quest> getQuests() {
        return plugin.getQuestManager().getAllQuests().values();
    }

    public QuestProgress getProgress(UUID playerUuid, String questId) {
        return plugin.getQuestManager().getProgress(playerUuid, questId);
    }

    protected void shutdown() {}

    public NexoriaQuestsPlugin getPlugin() {
        return plugin;
    }
}
