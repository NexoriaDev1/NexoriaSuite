package dev.nexoria.quests;

public final class Quest {

    public enum QuestType {
        KILL_MOB,
        MINE_BLOCK
    }

    private final String id;
    private final String name;
    private final String description;
    private final QuestType type;
    private final String target;
    private final int requiredAmount;
    private final double rewardMoney;
    private final int rewardXp;

    public Quest(String id, String name, String description, QuestType type, String target, int requiredAmount, double rewardMoney, int rewardXp) {
        this.id = id.toLowerCase();
        this.name = name;
        this.description = description;
        this.type = type;
        this.target = target != null ? target.toUpperCase() : "";
        this.requiredAmount = Math.max(1, requiredAmount);
        this.rewardMoney = Math.max(0.0, rewardMoney);
        this.rewardXp = Math.max(0, rewardXp);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public QuestType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public int getRewardXp() {
        return rewardXp;
    }
}
