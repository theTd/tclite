package com.mineclay.tclite.command;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@FunctionalInterface
public interface ChildCommand {

    void execute(@NotNull CommandContext ctx) throws CommandSignal;

    default List<String> tabComplete(@NotNull CommandContext ctx, String arg) throws CommandSignal {
        return Collections.emptyList();
    }
}
