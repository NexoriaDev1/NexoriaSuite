package dev.nexoria.skills;

import java.time.Instant;
import java.util.UUID;

public final class SkillsAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, int activeProfiles) {}

    private final NexoriaSkillsPlugin plugin;

    public SkillsAPI(NexoriaSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return plugin.isEnabled();
    }

    public ServiceHealth getStatus() {
        int count = plugin.getSkillManager() != null ? plugin.getSkillManager().getAllProfiles().size() : 0;
        return new ServiceHealth("NexoriaSkills", isActive(), Instant.now(), count);
    }

    public int getSkillLevel(UUID playerUuid, SkillType skill) {
        return plugin.getSkillManager().getProfile(playerUuid).getLevel(skill);
    }

    public double getSkillExp(UUID playerUuid, SkillType skill) {
        return plugin.getSkillManager().getProfile(playerUuid).getExp(skill);
    }

    public void awardExp(org.bukkit.entity.Player player, SkillType skill, double amount) {
        plugin.getSkillManager().awardExp(player, skill, amount);
    }

    protected void shutdown() {}

    public NexoriaSkillsPlugin getPlugin() {
        return plugin;
    }
}
