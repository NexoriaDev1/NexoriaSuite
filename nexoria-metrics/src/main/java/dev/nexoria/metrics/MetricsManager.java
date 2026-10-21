package dev.nexoria.metrics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.management.ManagementFactory;

public class MetricsManager {

    private final NexoriaMetricsPlugin plugin;
    private final long startTime;

    public MetricsManager(NexoriaMetricsPlugin plugin) {
        this.plugin = plugin;
        this.startTime = System.currentTimeMillis();
    }

    public MetricsSnapshot getSnapshot() {
        double[] tps = Bukkit.getTPS();
        double mspt = Bukkit.getAverageTickTime();

        Runtime runtime = Runtime.getRuntime();
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        long totalMem = runtime.totalMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;

        int threads = Thread.activeCount();
        long uptime = System.currentTimeMillis() - startTime;

        return new MetricsSnapshot(tps, mspt, usedMem, maxMem, threads, uptime);
    }

    public String formatTPS(double tps) {
        double val = Math.min(20.0, Math.round(tps * 100.0) / 100.0);
        if (val >= 19.5) {
            return ChatColor.GREEN + String.format("%.2f", val);
        } else if (val >= 17.0) {
            return ChatColor.YELLOW + String.format("%.2f", val);
        } else {
            return ChatColor.RED + String.format("%.2f", val);
        }
    }

    public String formatMSPT(double mspt) {
        double val = Math.round(mspt * 100.0) / 100.0;
        if (val <= 40.0) {
            return ChatColor.GREEN + String.format("%.2f", val) + "ms";
        } else if (val <= 50.0) {
            return ChatColor.YELLOW + String.format("%.2f", val) + "ms";
        } else {
            return ChatColor.RED + String.format("%.2f", val) + "ms";
        }
    }

    public String formatUptime(long millis) {
        long sec = millis / 1000L;
        long min = sec / 60;
        long remSec = sec % 60;
        long hours = min / 60;
        long remMin = min % 60;
        long days = hours / 24;
        long remHours = hours % 24;

        if (days > 0) return days + "d " + remHours + "h " + remMin + "m";
        if (hours > 0) return hours + "h " + remMin + "m " + remSec + "s";
        return min + "m " + remSec + "s";
    }

    public void runGC() {
        System.gc();
    }
}
