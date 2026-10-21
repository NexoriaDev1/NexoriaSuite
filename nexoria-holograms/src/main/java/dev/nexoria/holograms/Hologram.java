package dev.nexoria.holograms;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Hologram {

    private final String name;
    private Location location;
    private final List<String> lines = new ArrayList<>();
    private final List<UUID> spawnedEntities = new ArrayList<>();

    public Hologram(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public void spawn(double lineSpacing, Display.Billboard billboard) {
        despawn();
        if (location == null || location.getWorld() == null || lines.isEmpty()) {
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Location lineLoc = location.clone().subtract(0, i * lineSpacing, 0);

            try {
                TextDisplay display = location.getWorld().spawn(lineLoc, TextDisplay.class, entity -> {
                    entity.text(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
                    entity.setBillboard(billboard);
                    entity.setDefaultBackground(false);
                    entity.setSeeThrough(false);
                    entity.setShadowed(true);
                    entity.addScoreboardTag("nexoria_holo_" + name);
                });
                spawnedEntities.add(display.getUniqueId());
            } catch (Throwable ignored) {}
        }
    }

    public void despawn() {
        for (UUID uuid : spawnedEntities) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) {
                e.remove();
            }
        }
        spawnedEntities.clear();
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getLines() {
        return lines;
    }

    public void addLine(String text) {
        lines.add(text);
    }

    public void setLine(int index, String text) {
        if (index >= 0 && index < lines.size()) {
            lines.set(index, text);
        }
    }
}
