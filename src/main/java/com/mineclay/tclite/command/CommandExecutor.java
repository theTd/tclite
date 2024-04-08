package com.mineclay.tclite.command;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class CommandExecutor implements org.bukkit.command.CommandExecutor, TabCompleter, ChildCommand {
    final Map<String, CommandExecutor> childMap = new LinkedHashMap<>();
    public final String label;
    private final CommandExecutor parent;
    public String permission;
    public String permissionMessage;
    public String description;

    protected void childCommand(String label, ChildCommand executor) {
        new CommandExecutor(this, label) {
            @Override
            public void execute(@NotNull CommandContext ctx) throws CommandSignal {
                executor.execute(ctx);
            }

            @Override
            @Nullable
            public List<String> tabComplete(@NotNull CommandContext ctx, String arg) throws CommandSignal {
                return executor.tabComplete(ctx, arg);
            }
        };
    }

    public boolean permissionDenied(CommandSender sender) {
        return permission != null && !sender.hasPermission(permission);
    }

    private final List<ArgHandler<?>> args = new ArrayList<>();

    protected static CommandSignal escapeToHelpMessage() {
        return CommandSignal.ESCAPE_HELP;
    }

    protected static CommandSignal error(String message) {
        return new CommandSignal(message);
    }

    protected static CommandSignal argError(int argPos) {
        return new CommandSignal.ArgError(argPos, null);
    }

    protected static CommandSignal argError(int argPos, String message) {
        return new CommandSignal.ArgError(argPos, message);
    }

    protected static CommandSignal argError(String message) {
        return new CommandSignal.ArgError(null, message);
    }

    protected @NotNull ArgTokenR<String> requireArg() {
        return requireArg(ArgParser.STRING, "");
    }

    protected @NotNull ArgTokenR<String> requireArg(@Nullable String name) {
        return requireArg(ArgParser.STRING, name);
    }

    protected <T> @NotNull ArgTokenR<T> requireArg(@NotNull ArgParser<T> parser) {
        return requireArg(parser, null);
    }

    protected <T> @NotNull ArgTokenR<T> requireArg(@NotNull ArgParser<T> parser, @Nullable String name) {
        RequiredArgHandler<T> handler = new RequiredArgHandler<>(parser, name);
        args.add(handler);
        return handler;
    }

    protected @NotNull ArgTokenO<String> optionalArg() {
        return optionalArg(ArgParser.STRING, "");
    }

    protected @NotNull ArgTokenO<String> optionalArg(String name) {
        return optionalArg(ArgParser.STRING, name);
    }

    protected <T> @NotNull ArgTokenO<T> optionalArg(@NotNull ArgParser<T> parser) {
        return optionalArg(parser, "");
    }

    private static class OptionalArgHandler<T> extends RawArgHandler<T> implements ArgTokenO<T> {
        public OptionalArgHandler(ArgParser<T> parser, String name) {
            super(parser, name, true);
        }

        @Override
        public ArgTokenO<T> defaultsTo(@NotNull T defaultValue) {
            return (ArgTokenO<T>) super.defaultsTo(defaultValue);
        }

        @Override
        public @NotNull <U> ArgTokenO<U> parser(@NotNull InlineParser<U> parser) {
            return (ArgTokenO<U>) super.parser(parser);
        }

        @Override
        public @NotNull ArgTokenO<T> completor(@NotNull InlineCompletor completor) {
            return (ArgTokenO<T>) super.completor(completor);
        }

        @Override
        public @NotNull ArgTokenO<T> description(@Nullable String description) {
            return (ArgTokenO<T>) super.description(description);
        }
    }

    private static class RequiredArgHandler<T> extends RawArgHandler<T> implements ArgTokenO<T> {
        public RequiredArgHandler(ArgParser<T> parser, String name) {
            super(parser, name, false);
        }

        @Override
        public ArgTokenR<T> defaultsTo(@NotNull T defaultValue) {
            return (ArgTokenR<T>) super.defaultsTo(defaultValue);
        }

        @Override
        public @NotNull <U> ArgTokenO<U> parser(@NotNull InlineParser<U> parser) {
            return (ArgTokenO<U>) super.parser(parser);
        }

        @Override
        public @NotNull ArgTokenO<T> completor(@NotNull InlineCompletor completor) {
            return (ArgTokenO<T>) super.completor(completor);
        }

        @Override
        public @NotNull ArgTokenO<T> description(@Nullable String description) {
            return (ArgTokenO<T>) super.description(description);
        }
    }

    private static class RawArgHandler<T> implements ArgHandler<T>, ArgToken<T> {
        @NotNull
        final String name;
        @NotNull
        InlineParser<T> parser;
        @Nullable
        InlineCompletor completor;
        @Nullable
        T defaultValue;
        @Nullable
        String description;

        boolean optional;

        public RawArgHandler(ArgParser<T> parser, String name, boolean optional) {
            this.parser = parser::parse;
            this.name = name != null ? name : parser.getName() != null ? parser.getName() : "";
            completor = parser::complete;
            this.optional = optional;
        }

        private RawArgHandler(@NotNull InlineParser<T> parser, @Nullable InlineCompletor completor, String name, boolean optional) {
            this.parser = parser;
            this.completor = completor;
            this.name = name == null ? "" : name;
            this.optional = optional;
        }

        @Override
        public @NotNull <U> ArgToken<U> parser(@NotNull InlineParser<U> parser) {
            defaultValue = null;
            //noinspection unchecked
            RawArgHandler<U> h = (RawArgHandler<U>) this;
            h.parser = parser;
            return h;
        }

        @Override
        public @NotNull ArgToken<T> completor(@NotNull InlineCompletor completor) {
            this.completor = completor;
            return this;
        }

        @Override
        public @NotNull ArgToken<T> description(@Nullable String description) {
            this.description = description;
            return this;
        }

        public ArgToken<T> defaultsTo(@NotNull T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }

        @Override
        public @Nullable T getDefaultValue() {
            return defaultValue;
        }

        @Override
        public @NotNull T parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return parser.parse(ctx, arg);
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return completor == null ? Collections.emptyList() : completor.complete(ctx, arg);
        }

        @Override
        public @Nullable String getDescription() {
            return description;
        }
    }

    protected <T> @NotNull ArgTokenO<T> optionalArg(@NotNull ArgParser<T> parser, @Nullable String name) {
        OptionalArgHandler<T> handler = new OptionalArgHandler<>(parser, name);
        args.add(handler);
        return handler;
    }

    protected CommandExecutor(@NotNull String label) {
        this(null, label);
    }

    protected CommandExecutor(@Nullable CommandExecutor parent, @NotNull String label) {
        if (label.isEmpty())
            throw new IllegalArgumentException("invalid command label");

        this.parent = parent;
        this.label = label;
        if (this.parent != null) {
            parent.addChild(this);
        }
    }

    public @NotNull String getLabel() {
        return label;
    }

    public @Nullable CommandExecutor getParent() {
        return parent;
    }

    public @NotNull List<CommandExecutor> getChild() {
        return Collections.unmodifiableList(new ArrayList<>(childMap.values()));
    }

    private void addChild(@NotNull CommandExecutor executor) {
        Preconditions.checkNotNull(executor, "executor is null");
        CommandExecutor exists = this.childMap.get(executor.label);
        if (exists != null) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "swapping executor from " +
                    exists.getClass().getName() + " to " + executor.getClass().getName());
        }
        this.childMap.put(executor.label.toLowerCase(), executor);
    }

    public abstract void execute(@NotNull CommandContext ctx) throws CommandSignal;

    private static CommandContext createCtx(CommandSender sender, String label, String[] parts, LinkedList<String> args, Map<ArgHandler<?>, Object> resolve) {
        return new CommandContext() {
            private Player player;

            {
                if (sender instanceof Player) {
                    player = (Player) sender;
                }
            }

            @Override
            public @NotNull CommandSender getSender() {
                return sender;
            }

            @Override
            public @Nullable Player getPlayer() {
                return player;
            }

            @Override
            public @NotNull String getCommandLine() {
                String argsPart = String.join(" ", parts);
                return label + (argsPart.isEmpty() ? "" : " " + argsPart);
            }

            @Override
            public @NotNull String[] getArgs() {
                return args.toArray(new String[0]);
            }

            @Override
            public @NotNull String[] getOriginalArgs() {
                return parts;
            }

            @Override
            public @NotNull String getAllArgs() {
                return String.join(" ", args);
            }

            @Override
            public <T> @Nullable T valueOf(@NotNull ArgToken<T> token) {
                //noinspection SuspiciousMethodCalls,unchecked
                return (T) resolve.get(token);
            }

            @Override
            public <T> @NotNull T valueOf(@NotNull ArgTokenR<T> token) {
                // noinspection SuspiciousMethodCalls,unchecked
                return (T) resolve.get(token);
            }

            @Override
            public <T> @Nullable T valueOf(@NotNull String arg) {
                Map.Entry<ArgHandler<?>, Object> ent = resolve.entrySet().stream().filter(e -> e.getKey().getName().equals(arg)).findFirst().orElse(null);
                if (ent == null) return null;
                //noinspection unchecked
                return (T) ent.getValue();
            }
        };
    }

    @Override
    public final boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] parts) {
        AtomicInteger idx = new AtomicInteger(0);
        CommandExecutor cmd;
        try {
            cmd = findCommand(sender, parts, idx);
        } catch (CommandSignal e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            return true;
        }

        LinkedList<String> args = new LinkedList<>(Arrays.asList(parts).subList(idx.get(), parts.length));
        Map<ArgHandler<?>, Object> resolve = new LinkedHashMap<>();
        CommandContext ctx = createCtx(sender, label, parts, args, resolve);

        try {
            for (ArgHandler<?> arg : cmd.args) {
                String toParse;
                if (args.isEmpty()) {
                    toParse = null;
                } else {
                    toParse = args.removeFirst();
                }
                if (toParse == null) {
                    if (arg.isOptional()) {
                        resolve.put(arg, arg.getDefaultValue());
                    } else {
                        throw escapeToHelpMessage();
                    }
                } else {
                    resolve.put(arg, arg.parse(ctx, toParse));
                }
            }
            cmd.execute(ctx);
        } catch (CommandSignal e) {
            if (e == CommandSignal.ESCAPE_HELP) {
                cmd.showAllHelp(ctx, true);
            } else if (e instanceof CommandSignal.ArgError) {
                cmd.showArgError(ctx, ((CommandSignal.ArgError) e).argPos, e.getMessage());
            } else {
                sender.sendMessage(ChatColor.RED + e.getMessage());
            }
        }
        return true;
    }

    public void showHelp(CommandContext ctx) {
        StringBuilder thisCommand = new StringBuilder(getCommandPath());
        for (ArgHandler<?> arg : args) {
            if (arg.isOptional()) {
                thisCommand.append(" [").append(arg.getName()).append("]");
            } else {
                thisCommand.append(" <").append(arg.getName()).append(">");
            }
        }
        ctx.getSender().sendMessage(ChatColor.GOLD + "/" + thisCommand + (description != null ? " - " + description : ""));
    }

    private void showAllHelp(CommandContext ctx, boolean child) {
        if (permissionDenied(ctx.getSender())) return;
        showHelp(ctx);
        if (child) {
            for (CommandExecutor ch : childMap.values()) {
                ch.showAllHelp(ctx, false);
            }
        }
    }

    private void showArgError(CommandContext ctx, Integer argPos, String message) {
        if (argPos != null) {
            if (ctx.getArgs().length <= argPos) {
                ctx.getSender().sendMessage(ChatColor.RED + message);
                return;
            }

            LinkedList<String> parts = new LinkedList<>(Arrays.stream(ctx.getCommandLine().split(" ")).collect(Collectors.toList()));
            int dropLast = ctx.getArgs().length - argPos - 1;
            parts.subList(0, parts.size() - dropLast).clear();
            String heading = Joiner.on(" ").join(parts);
            ctx.getSender().sendMessage(ChatColor.RED + heading + "<-" + (message != null ? " " + message : ""));
        }
        showAllHelp(ctx, true);
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] parts) {
        AtomicInteger idx = new AtomicInteger(0);
        CommandExecutor cmd;
        try {
            cmd = findCommand(sender, parts, idx);
        } catch (CommandSignal e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            return Collections.emptyList();
        }

        LinkedList<String> args = new LinkedList<>(Arrays.asList(parts).subList(idx.get(), parts.length));
        if (args.isEmpty()) return Collections.emptyList();

        Map<ArgHandler<?>, Object> resolve = new LinkedHashMap<>();
        CommandContext ctx = createCtx(sender, label, parts, args, resolve);

        try {
            Set<String> result = new LinkedHashSet<>();

            // child
            assert args.peek() != null;
            String toComplete = args.peek().toLowerCase(Locale.ROOT);
            cmd.childMap.entrySet().stream().filter(s -> s.getKey().toLowerCase(Locale.ROOT).startsWith(toComplete) && !s.getValue().permissionDenied(sender)).forEach(e -> result.add(e.getKey()));

            // resolve args
            for (ArgHandler<?> arg : cmd.args) {
                if (args.isEmpty()) return new ArrayList<>(result);
                String toParse = args.removeFirst();
                boolean last = args.isEmpty();
                if (!last) {
                    resolve.put(arg, arg.parse(ctx, toParse));
                } else {
                    arg.complete(ctx, "").forEach(result::add);
                    break;
                }
            }

            if (args.isEmpty()) return new ArrayList<>(result);
            String lastArg = args.peekLast();
            List<String> complete = cmd.tabComplete(ctx, lastArg);
            if (complete != null) {
                result.addAll(complete);
            } else if (result.isEmpty()) return null;

            return new ArrayList<>(result);
        } catch (CommandSignal e) {
            if (e == CommandSignal.ESCAPE_HELP) {
                showAllHelp(ctx, true);
            } else if (e instanceof CommandSignal.ArgError) {
                showArgError(ctx, ((CommandSignal.ArgError) e).argPos, e.getMessage());
            } else {
                sender.sendMessage(ChatColor.RED + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    @Nullable
    public List<String> tabComplete(@NotNull CommandContext ctx, String arg) throws CommandSignal {
        return null;
    }

    public @NotNull String getCommandPath() {
        Stack<String> s = new Stack<>();
        CommandExecutor e = this;
        while (e != null) {
            s.push(e.getLabel());
            e = e.getParent();
        }
        StringBuilder sb = new StringBuilder();
        while (!s.isEmpty()) {
            sb.append(s.pop());
            if (!s.isEmpty()) sb.append(" ");
        }
        return sb.toString();
    }

    public void register(@NotNull JavaPlugin plugin) {
        try {
            PluginCommand cmd = plugin.getCommand(label);
            if (cmd == null) {
                Constructor<PluginCommand> cons = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                cons.setAccessible(true);
                cmd = cons.newInstance(label, plugin);
                Field commandMap = plugin.getServer().getClass().getDeclaredField("commandMap");
                commandMap.setAccessible(true);
                CommandMap map = (CommandMap) commandMap.get(plugin.getServer());
                map.register(plugin.getName(), cmd);
                try {
                    plugin.getServer().getClass().getDeclaredMethod("syncCommands").invoke(plugin.getServer());
                } catch (NoSuchMethodException ignored) {
                }
            }
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private CommandExecutor findCommand(CommandSender sender, String[] parts, AtomicInteger pos) throws CommandSignal {
        if (permissionDenied(sender)) {
            if (permissionMessage != null) {
                throw error(permissionMessage);
            } else {
                throw error("permission denied");
            }
        }

        String label = parts == null || parts.length == 0 ? null : parts[0].toLowerCase();
        if (label != null) {
            // find match child
            CommandExecutor child = childMap.get(label);
            if (child == null) {
                // find without dash
                child = childMap.entrySet().stream().filter(e -> e.getKey().replace("-", "").equals(label)).map(Map.Entry::getValue).findFirst().orElse(null);
            }
            if (child != null) {
                pos.incrementAndGet();
                String[] newParts = new String[parts.length - 1];
                System.arraycopy(parts, 1, newParts, 0, newParts.length);
                return child.findCommand(sender, newParts, pos);
            }
        }
        return this;
    }
}
