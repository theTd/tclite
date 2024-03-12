package com.mineclay.tclite;

import com.mineclay.tclite.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandDemo extends CommandExecutor {
    final ArgTokenR<Integer> page = optionalArg(ArgParser.INTEGER).defaultsTo(1);

    protected CommandDemo() {
        super("demo");
        childCommand("sub", new ChildCommand() {

            @Override
            public void execute(@NotNull CommandContext ctx) {

            }
        });
        new CommandExecutor(this, "sub-command") {

            final ArgTokenR<Integer> page = optionalArg(ArgParser.INTEGER, "page").parser((ctx, arg) -> {
                int page = Integer.parseInt(arg);
                if (page < 1) throw error("invalid page");
                return page;
            }).description("page of result").defaultsTo(1);

            @Override
            public void execute(@NotNull CommandContext ctx) throws CommandSignal {
                ctx.getSender().sendMessage("page is " + ctx.valueOf(page));
            }

            @Override
            @Nullable
            public List<String> tabComplete(@NotNull CommandContext ctx, String arg) throws CommandSignal {
                return Stream.of("Alice", "Bob", "Charlie").filter(s -> s.toLowerCase(Locale.ROOT).startsWith(arg.toLowerCase())).collect(Collectors.toList());
            }
        };
    }

    @Override
    public void execute(@NotNull CommandContext ctx) throws CommandSignal {
        ctx.getSender().sendMessage("Hello, world! page=" + ctx.valueOf(page));
    }
}
