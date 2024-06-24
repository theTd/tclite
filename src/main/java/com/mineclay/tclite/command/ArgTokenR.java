package com.mineclay.tclite.command;

import org.jetbrains.annotations.NotNull;

public interface ArgTokenR<T> extends ArgToken<T> {
    @Override
    @NotNull <U> ArgTokenO<U> parser(@NotNull InlineParser<U> parser);

    @Override
    @NotNull
    <U> ArgTokenO<U> parserOrAsync(@NotNull InlineParser<U> parser);

    @Override
    @NotNull
    ArgTokenR<T> completor(@NotNull InlineCompletor completor);

    @Override
    @NotNull
    ArgTokenR<T> completorOrAsync(@NotNull InlineCompletor completor);

    @Override
    @NotNull
    ArgTokenR<T> description(String description);
}
