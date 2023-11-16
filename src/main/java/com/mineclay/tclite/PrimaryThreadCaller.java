package com.mineclay.tclite;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class PrimaryThreadCaller<T> {
    final JavaPlugin plugin;

    public PrimaryThreadCaller(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    protected abstract T syncCall();

    public T call() throws ExecutionException, InterruptedException {
        if (Bukkit.isPrimaryThread()) return syncCall();

        CompletableFuture<T> f = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                f.complete(syncCall());
            } catch (Exception e) {
                f.completeExceptionally(e);
            }
        });
        return f.get();
    }
}
