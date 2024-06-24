package com.mineclay.tclite.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ArgToken<T> {
    default boolean isOptional() {
        return false;
    }

    @FunctionalInterface
    interface InlineParser<T> {
        @NotNull T parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal;

        default void error(String error) throws CommandSignal {
            throw new CommandSignal(error);
        }
    }

    @FunctionalInterface
    interface InlineCompletor {
        @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal;

        default void error(String error) throws CommandSignal {
            throw new CommandSignal(error);
        }
    }

    @NotNull <U> ArgToken<U> parser(@NotNull InlineParser<U> parser);

    /**
     * parser will support async operation
     *
     * @param parser parser
     * @param <U>    return type
     * @return new ArgToken
     */
    @NotNull
    <U> ArgToken<U> parserOrAsync(@NotNull InlineParser<U> parser);

    @NotNull ArgToken<T> completor(@NotNull InlineCompletor completor);

    /**
     * completor will support async operation
     *
     * @param completor completor
     * @return new ArgToken
     */
    @NotNull
    ArgToken<T> completorOrAsync(@NotNull InlineCompletor completor);

    @NotNull ArgToken<T> description(@Nullable String description);
}
