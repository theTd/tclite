package com.mineclay.tclite.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface ArgHandler<T> {
    @NotNull String getName();

    boolean isOptional();

    @Nullable T getDefaultValue();

    @NotNull T parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal;

    @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal;

    @Nullable String getDescription();
}
