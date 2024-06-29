package com.mineclay.tclite;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public abstract class PrimaryThreadCaller<T> {
    final JavaPlugin plugin;
    final Executor executor;

    public PrimaryThreadCaller(JavaPlugin plugin) {
        this.plugin = plugin;
        executor = (t) -> Bukkit.getScheduler().runTask(plugin, t);
    }

    protected abstract T syncCall();

    public T call() throws ExecutionException, InterruptedException {
        if (Bukkit.isPrimaryThread()) return syncCall();
        return callAsync().get();
    }

    public CompletableFuture<T> callAsync() {
        return CompletableFuture.supplyAsync(this::syncCall, executor);
    }
}
