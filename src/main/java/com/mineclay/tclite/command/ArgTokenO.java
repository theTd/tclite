package com.mineclay.tclite.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ArgTokenO<T> extends ArgTokenR<T> {
    @Override
    default boolean isOptional() {
        return true;
    }

    ArgTokenR<T> defaultsTo(@NotNull T defaultValue);

    @Override
    @NotNull <U> ArgTokenO<U> parser(@NotNull InlineParser<U> parser);

    @Override
    @NotNull
    ArgTokenO<T> completor(@NotNull InlineCompletor completor);

    @Override
    @NotNull
    ArgTokenO<T> description(@Nullable String description);
}
