package com.mineclay.tclite.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandContext {
    @NotNull CommandSender getSender();

    @Nullable Player getPlayer();

    @NotNull String getCommandLine();

    @NotNull String[] getArgs();

    @NotNull String[] getOriginalArgs();

    @NotNull String getAllArgs();

    <T> @Nullable T valueOf(@NotNull ArgToken<T> token);

    <T> @NotNull T valueOf(@NotNull ArgTokenR<T> token);

    <T> @Nullable T valueOf(@NotNull String arg);
}
