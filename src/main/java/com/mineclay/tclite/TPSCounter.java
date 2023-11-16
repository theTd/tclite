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
        long start = now - windowSecs * 1000L;
        int count = 0;
        for (long tick : tickDeque) {
            if (tick >= start) count++;
            else break;
        }
        return count / (double) windowSecs;
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
    private final Deque<Long> tickDeque = new ArrayDeque<>(BUFFER_SIZE);

    private void update() {
        if (tickDeque.size() >= BUFFER_SIZE)
            tickDeque.removeFirst();
        tickDeque.addLast(System.currentTimeMillis());
    }
}
