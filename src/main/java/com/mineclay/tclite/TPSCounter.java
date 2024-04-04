package com.mineclay.tclite;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.Deque;

public class TPSCounter implements Loader.Loadable {
    final Plugin plugin;

    public TPSCounter(Plugin plugin) {
        this.plugin = plugin;
    }

    public double getTPS(int windowSecs) {
        long now = System.currentTimeMillis();
        long lim = now - windowSecs * 1000L;
        int tickCount = 0;
        long lastTick = now;
        for (Long l : tickDeque) {
            if (l < lim) break;
            tickCount++;
            lastTick = l;
        }
        return tickCount / ((now - lastTick) / 1000.0);
    }

    BukkitTask task;

    @Override
    public void load() throws Exception {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 1, 1);
    }

    @Override
    public void unload() {
        if (task != null) task.cancel();
    }

    private final static int BUFFER_SIZE = 20 * 60 * 15;
    protected final Deque<Long> tickDeque = new ArrayDeque<>();

    private void update() {
        tickDeque.addFirst(System.currentTimeMillis());
        if (tickDeque.size() > BUFFER_SIZE) tickDeque.removeLast();
    }
}
