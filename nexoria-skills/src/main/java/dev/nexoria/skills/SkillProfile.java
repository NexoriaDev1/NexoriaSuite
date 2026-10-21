package dev.nexoria.skills;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class SkillProfile {

    private final UUID playerUuid;
    private final Map<SkillType, Integer> levels = new EnumMap<>(SkillType.class);
    private final Map<SkillType, Double> exp = new EnumMap<>(SkillType.class);

    public SkillProfile(UUID playerUuid) {
        this.playerUuid = playerUuid;
        for (SkillType s : SkillType.values()) {
            levels.put(s, 1);
            exp.put(s, 0.0);
        }
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public int getLevel(SkillType skill) {
        return levels.getOrDefault(skill, 1);
    }

    public void setLevel(SkillType skill, int level) {
        levels.put(skill, Math.max(1, level));
    }

    public double getExp(SkillType skill) {
        return exp.getOrDefault(skill, 0.0);
    }

    public void setExp(SkillType skill, double xp) {
        exp.put(skill, Math.max(0.0, xp));
    }

    public double getRequiredExp(SkillType skill) {
        int lvl = getLevel(skill);
        // Formula: 100 * (1.35 ^ (lvl - 1))
        return Math.floor(100.0 * Math.pow(1.35, lvl - 1));
    }

    public boolean addExp(SkillType skill, double amount) {
        double current = getExp(skill) + amount;
        double req = getRequiredExp(skill);
        boolean leveledUp = false;

        while (current >= req && getLevel(skill) < 100) {
            current -= req;
            setLevel(skill, getLevel(skill) + 1);
            leveledUp = true;
            req = getRequiredExp(skill);
        }

        setExp(skill, current);
        return leveledUp;
    }
}
