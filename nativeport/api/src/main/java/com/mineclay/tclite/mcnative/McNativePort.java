package com.mineclay.tclite.mcnative;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface McNativePort {
    void sendTitle(Player player, String title, String subtitle, int fadeIn, int keep, int fadeOut);

    void sendTitle(Player player, String title);

    void sendSubtitle(Player player, String subtitle);

    void clearTitle(Player player);

    void resetTitle(Player player);

    void setTimes(Player player, int fadeIn, int keep, int fadeOut);

    int getStateId(Player player);

    int getActiveWindowId(Player player);

    void sendSlotChange(int windowId, int slot, ItemStack item, Player p);

    @Nullable
    default AsyncTabCompleteEventSocket adaptAsyncTabCompleteEvent(@NotNull Event event) {
        return null;
    }
}
