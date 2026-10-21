package dev.nexoria.skills;

public enum SkillType {
    MINING("Mining", "Mine ores and stone to uncover riches"),
    WOODCUTTING("Woodcutting", "Harvest timber and master the forests"),
    FARMING("Farming", "Cultivate crops and reap bountiful harvests"),
    COMBAT("Combat", "Vanquish hostile beasts and master warfare"),
    FISHING("Fishing", "Cast your line and haul aquatic treasures");

    private final String displayName;
    private final String description;

    SkillType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static SkillType fromString(String name) {
        for (SkillType s : values()) {
            if (s.name().equalsIgnoreCase(name) || s.displayName.equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }
}
