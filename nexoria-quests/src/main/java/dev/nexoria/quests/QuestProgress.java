package dev.nexoria.quests;

import java.util.UUID;

public final class QuestProgress {

    private final UUID playerUuid;
    private final String questId;
    private int currentProgress;
    private boolean completed;
    private long completedAt;

    public QuestProgress(UUID playerUuid, String questId, int currentProgress, boolean completed, long completedAt) {
        this.playerUuid = playerUuid;
        this.questId = questId;
        this.currentProgress = currentProgress;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getQuestId() {
        return questId;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}
