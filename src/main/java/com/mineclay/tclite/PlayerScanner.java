package com.mineclay.tclite;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

public abstract class PlayerScanner implements Runnable {
    private final Map<String, Integer> map = new HashMap<>();
    private final int maxIntervalTick;
    private int tick = 0;

    public PlayerScanner(int maxIntervalTick) {
        this.maxIntervalTick = maxIntervalTick;
    }

    @Override
    public void run() {
        int times = new BigDecimal(Bukkit.getOnlinePlayers().size() / ((double) maxIntervalTick)).setScale(0, RoundingMode.UP).intValue();
        for (int i = 0; i < times; i++) {
            Player next = null;
            int leastTick = -1;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (shouldHandle(p) && !map.containsKey(p.getName())) {
                    next = p;
                    break;
                }
            }

            if (next == null) {
                Iterator<Map.Entry<String, Integer>> ite = map.entrySet().iterator();
                while (ite.hasNext()) {
                    Map.Entry<String, Integer> en = ite.next();
                    Player p = Bukkit.getPlayerExact(en.getKey());
                    if (p == null) {
                        ite.remove();
                        continue;
                    }

                    if (!shouldHandle(p)) continue;

                    if (next == null) {
                        next = p;
                        leastTick = en.getValue();
                    } else {
                        if (en.getValue() < leastTick) {
                            next = p;
                            leastTick = en.getValue();
                        }
                    }
                }

                if (tick - leastTick < 20) next = null;

            }

            if (next != null) {
                try {
                    handle(next);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "exception in player scanner", e);
                }
                map.put(next.getName(), tick);
            }
        }
        tick += 1;
    }

    public abstract void handle(Player player);

    public boolean shouldHandle(Player player) {
        return true;
    }

    private BukkitTask task;
    private JavaPlugin plugin;

    public void start(JavaPlugin plugin) {
        this.plugin = plugin;
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this, 1, 1);
    }

    public void stop() {
        if (task != null && (Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId()) || Bukkit.getScheduler().isQueued(task.getTaskId()))) {
            task.cancel();
            task = null;
        }
    }
}
