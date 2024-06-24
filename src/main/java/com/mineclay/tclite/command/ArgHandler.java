package com.mineclay.tclite.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

interface ArgHandler<T> {
    @NotNull String getName();

    boolean isOptional();

    @Nullable
    Function<CommandContext, T> getDefaultValueSupplier();

    @NotNull T parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal;

    default @NotNull T asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
        throw CommandSignal.NOT_IMPLEMENTED;
    }

    @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal;

    default @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
        throw CommandSignal.NOT_IMPLEMENTED;
    }

    @Nullable String getDescription();
}
