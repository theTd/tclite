package com.mineclay.tclite.ui.playerinv;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class PlayerInvButtonInitializer<T extends PlayerInvButton> {
    private final int slot;
    private final boolean hotbar;

    protected PlayerInvButtonInitializer(int slot, boolean hotbar) {
        this.slot = slot;
        this.hotbar = hotbar;
    }

    protected PlayerInvButtonInitializer(int slot) {
        this(slot, false);
    }

    public abstract T create(Player player);

    public void register(JavaPlugin plugin) {
        PlayerInvUiService.inst().register(this, plugin);
    }

    public void unregister() {
        PlayerInvUiService.inst().unregister(this);
    }

    PlayerInvButtonSlot getSlotMark() {
        return new PlayerInvButtonSlot(slot, hotbar);
    }
}
