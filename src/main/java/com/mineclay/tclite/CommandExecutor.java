package com.mineclay.tclite;

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

public abstract class CommandExecutor implements org.bukkit.command.CommandExecutor, TabCompleter {
    final Map<String, CommandExecutor> childMap = new LinkedHashMap<>();
    private final String label;
    private final CommandExecutor parent;

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

    private JavaPlugin owner;

    private @NotNull Logger logger() {
        if (owner != null) {
            return owner.getLogger();
        } else {
            return Logger.getLogger(getClass().getName());
        }
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] parts) {
        AtomicInteger idx = new AtomicInteger(0);
        CommandExecutor cmd = findCommand(parts, idx);
        String[] args = new String[parts.length - idx.get()];
        System.arraycopy(parts, idx.get(), args, 0, args.length);
        try {
            cmd.execute(sender, sender instanceof Player ? (Player) sender : null, args);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "internal error");
            logger().log(Level.SEVERE, "exception executing command", e);
        }
        return true;
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] parts) {
        AtomicInteger idx = new AtomicInteger(0);
        CommandExecutor cmd = findCommand(parts, idx);
        String[] args = new String[parts.length - idx.get()];
        System.arraycopy(parts, idx.get(), args, 0, args.length);
        try {
            return cmd.tabComplete0(sender, args);
        } catch (Exception e) {
            logger().log(Level.SEVERE, "exception in tab-complete", e);
            return Collections.emptyList();
        }
    }

    public abstract void execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args);

    private List<String> tabComplete0(CommandSender sender, String[] args) {
        String pre = args.length == 0 ? "" : args[0].toLowerCase();
        List<String> lst = new LinkedList<>();
        if (!childMap.isEmpty()) {
            childMap.keySet().stream().filter(l -> l.toLowerCase().startsWith(pre)).forEach(lst::add);
        }
        List<String> r = tabComplete(sender, sender instanceof Player ? ((Player) sender) : null, args);
        if (r != null) lst.addAll(r);
        return lst;
    }

    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args) {
        return Collections.emptyList();
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
        owner = plugin;
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

    private CommandExecutor findCommand(String[] parts, AtomicInteger pos) {
        String label = parts == null || parts.length == 0 ? null : parts[0].toLowerCase();
        if (label != null) {
            // find match child
            CommandExecutor child = childMap.get(label);
            if (child != null) {
                pos.incrementAndGet();
                String[] newParts = new String[parts.length - 1];
                System.arraycopy(parts, 1, newParts, 0, newParts.length);
                return child.findCommand(newParts, pos);
            }
        }
        return this;
    }
}
