package com.mineclay.tclite.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ArgTokenO<T> extends ArgTokenR<T> {
    @Override
    default boolean isOptional() {
        return true;
    }

    @NotNull
    default ArgTokenR<T> defaultsTo(@NotNull T defaultValue) {
        return defaultValue(ctx -> defaultValue);
    }

    @NotNull
    default ArgTokenO<T> defaultsTo(@NotNull Supplier<T> defaultValueSupplier) {
        return defaultValue(ctx -> defaultValueSupplier.get());
    }

    @NotNull
    ArgTokenO<T> defaultValue(@NotNull Function<CommandContext, T> defaultValueSupplier);

    @Override
    @NotNull
    <U> ArgTokenO<U> parser(@NotNull InlineParser<U> parser);

    @Override
    @NotNull
    <U> ArgTokenO<U> parserOrAsync(@NotNull InlineParser<U> parser);

    @Override
    @NotNull
    ArgTokenO<T> completor(@NotNull InlineCompletor completor);

    @Override
    @NotNull
    ArgTokenO<T> completorOrAsync(@NotNull InlineCompletor completor);

    @Override
    @NotNull
    ArgTokenO<T> description(@Nullable String description);
}
