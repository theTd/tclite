package com.mineclay.tclite;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AsyncTabCompleteEventSocket {

    @NotNull
    CommandSender getSender();

    @NotNull
    String getBuffer();

    boolean isCommand();

    @Nullable
    Location getLocation();

    @NotNull
    List<String> getCompletions();

    void setCompletions(@NotNull List<String> completions);

    boolean isHandled();

    void setHandled(boolean handled);

    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
