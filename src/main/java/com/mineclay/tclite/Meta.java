package com.mineclay.tclite;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public class Meta<T> {
    protected final String key;
    protected final Plugin owner;

    @Nullable
    public T get(@NotNull Player player) {
        MetadataValue meta = getMeta(player);
        if (meta == null) return null;
        try {
            //noinspection unchecked
            return (T) meta.value();
        } catch (ClassCastException e) {
            owner.getLogger().warning("invalidated metadata " + key + " for player " + player.getName());
            removeMeta(player);
            return null;
        }
    }

    public void set(@NotNull Player player, @Nullable T value) {
        setMeta(player, value);
    }

    protected void removeMeta(@NotNull Player player) {
        player.removeMetadata(key, owner);
    }

    protected void setMeta(@NotNull Player player, @Nullable Object value) {
        player.setMetadata(key, new FixedMetadataValue(owner, value));
    }

    protected @Nullable MetadataValue getMeta(@NotNull Player player) {
        return player.getMetadata(key).stream().filter(m -> m.getOwningPlugin() == owner).findFirst().orElse(null);
    }

    @NotNull
    public NonNull<T> defaultsTo(@NotNull T defaultValue) {
        return new NonNull<>(key, owner, (p) -> defaultValue);
    }

    @NotNull
    public NonNull<T> defaultsTo(@NotNull Function<Player, T> defaultValue) {
        return new NonNull<>(key, owner, defaultValue);
    }

    public static class NonNull<T> extends Meta<T> {
        final Function<Player, T> defaultValue;

        private NonNull(String key, Plugin owner, Function<Player, T> defaultValue) {
            super(key, owner);
            this.defaultValue = defaultValue;
        }

        @Override
        public @NotNull T get(@NotNull Player player) {
            MetadataValue value = player.getMetadata(key).stream().filter(m -> m.getOwningPlugin() == owner).findFirst().orElse(null);
            if (value == null) {
                T def = Objects.requireNonNull(defaultValue.apply(player), "default value must not be null");
                setMeta(player, def);
                return def;
            } else try {
                //noinspection unchecked
                return (T) value.value();
            } catch (ClassCastException e) {
                owner.getLogger().warning("invalidated metadata " + key + " for player " + player.getName());
                T def = Objects.requireNonNull(defaultValue.apply(player), "default value must not be null");
                setMeta(player, def);
                return def;
            }
        }

        @Override
        public void set(@NotNull Player player, @Nullable T value) {
            if (value == null) {
                removeMeta(player);
            } else {
                setMeta(player, value);
            }
        }
    }

    private final static Listener GLOBAL_LISTENER = new Listener() {
    };

    private final static Set<String> MANAGED_KEYS = Collections.synchronizedSet(new HashSet<>());

    private static void forManagedKeys(Consumer<String> action) {
        synchronized (MANAGED_KEYS) {
            MANAGED_KEYS.forEach(action);
        }
    }

    private static void registerPlugin(Plugin plugin) {
        PlayerQuitEvent.getHandlerList().register(new RegisteredListener(GLOBAL_LISTENER, (l, evt) -> {
            PlayerQuitEvent e = (PlayerQuitEvent) evt;
            forManagedKeys(key -> e.getPlayer().removeMetadata(key, plugin));
        }, EventPriority.MONITOR, plugin, false));

        PluginDisableEvent.getHandlerList().register(new RegisteredListener(GLOBAL_LISTENER, (l, evt) -> {
            PluginDisableEvent e = (PluginDisableEvent) evt;
            if (e.getPlugin() == plugin) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    forManagedKeys(key -> player.removeMetadata(key, e.getPlugin()));
                }
            }
        }, EventPriority.MONITOR, plugin, false));
    }

    /**
     * define a auto-removed bukkit metadata accessor
     */
    public static <T> Meta<T> define(Plugin plugin, String key) {
        if (MANAGED_KEYS.add(key) && Arrays.stream(PlayerQuitEvent.getHandlerList().getRegisteredListeners())
                .noneMatch(l -> l.getListener() == GLOBAL_LISTENER)) registerPlugin(plugin);
        return new Meta<>(key, plugin);
    }
}
