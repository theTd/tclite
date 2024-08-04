package com.mineclay.tclite.mcnative;

import com.mineclay.tclite.AsyncTabCompleteEventSocket;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.mineclay.tclite.mcnative.Implementation.IMPL;

public interface McNative {
    void sendTitle0(Player player, String title, String subtitle, int fadeIn, int keep, int fadeOut);

    static void sendTitle(Player player, String title, String subtitle, int fadeIn, int keep, int fadeOut) {
        IMPL.sendTitle0(player, title, subtitle, fadeIn, keep, fadeOut);
    }

    void sendTitle0(Player player, String title);

    static void sendTitle(Player player, String title) {
        IMPL.sendTitle0(player, title);
    }

    void sendSubtitle0(Player player, String subtitle);

    static void sendSubtitle(Player player, String subtitle) {
        IMPL.sendSubtitle0(player, subtitle);
    }

    void clearTitle0(Player player);

    static void clearTitle(Player player) {
        IMPL.clearTitle0(player);
    }

    void resetTitle0(Player player);

    static void resetTitle(Player player) {
        IMPL.resetTitle0(player);
    }

    void setTimes0(Player player, int fadeIn, int keep, int fadeOut);

    static void setTimes(Player player, int fadeIn, int keep, int fadeOut) {
        IMPL.setTimes0(player, fadeIn, keep, fadeOut);
    }

    int getStateId0(Player player);

    static int getStateId(Player player) {
        return IMPL.getStateId0(player);
    }

    int getActiveWindowId0(Player player);

    static int getActiveWindowId(Player player) {
        return IMPL.getActiveWindowId0(player);
    }

    void sendSlotChange0(int windowId, int slot, ItemStack item, Player p);

    static void sendSlotChange(int windowId, int slot, ItemStack item, Player p) {
        IMPL.sendSlotChange0(windowId, slot, item, p);
    }

    @Nullable
    default AsyncTabCompleteEventSocket adaptAsyncTabCompleteEvent0(@NotNull Event event) {
        return null;
    }

    static @Nullable AsyncTabCompleteEventSocket adaptAsyncTabCompleteEvent(@NotNull Event event) {
        return IMPL.adaptAsyncTabCompleteEvent0(event);
    }

    CommandMap getCommandMap0();

    static CommandMap getCommandMap() {
        return IMPL.getCommandMap0();
    }

    void syncCommands0();

    static void syncCommands() {
        IMPL.syncCommands0();
    }
}
