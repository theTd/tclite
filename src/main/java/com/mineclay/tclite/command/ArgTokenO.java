package com.mineclay.tclite.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface ArgTokenO<T> extends ArgTokenR<T> {
    @Override
    default boolean isOptional() {
        return true;
    }

    @NotNull
    ArgTokenR<T> defaultsTo(@NotNull T defaultValue);

    @NotNull
    ArgTokenO<T> defaultsTo(@NotNull Supplier<T> defaultValueSupplier);

    @Override
    @NotNull
    <U> ArgTokenO<U> parser(@NotNull InlineParser<U> parser);

    @Override
    @NotNull
    ArgTokenO<T> completor(@NotNull InlineCompletor completor);

    @Override
    @NotNull
    ArgTokenO<T> description(@Nullable String description);
}
