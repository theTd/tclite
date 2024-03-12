package com.mineclay.tclite.command;

import org.jetbrains.annotations.Nullable;

public class CommandSignal extends Throwable {
    public final static CommandSignal ESCAPE_HELP = new CommandSignal("");

    public CommandSignal(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }

    public static class ArgError extends CommandSignal {
        public final Integer argPos;

        public ArgError(@Nullable Integer argPos, @Nullable String message) {
            super(message);
            this.argPos = argPos;
        }
    }
}
