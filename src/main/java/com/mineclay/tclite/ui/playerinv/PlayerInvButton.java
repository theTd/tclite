package com.mineclay.tclite.ui.playerinv;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

public abstract class PlayerInvButton {
    @Getter
    Player player;
    @Getter
    PlayerInvButtonInitializer<?> initializer;
    private ItemStack item = new ItemStack(Material.AIR);

    public abstract PlayerInvResponse onHotbarClick(boolean rightClick);

    public abstract PlayerInvResponse onInvClick(InventoryAction action);

    public abstract void init();

    public void refresh() {
        // optional
    }

    public void update() {
        refresh();
        PlayerInvUiService.inst().update(this);
    }

    public void onPlayerQuit() {
        // optional
    }

    public ItemStack getItem() {
        return item == null ? null : item.clone();
    }

    protected void setItem(ItemStack item) {
        this.item = item;
    }

    public PlayerInvButtonSlot getSlotMark() {
        return initializer.getSlotMark();
    }

    public static <T extends PlayerInvButton> T getPlayerButton(Player player, Class<T> clz) {
        return PlayerInvUiService.inst().getPlayerButton(player, clz);
    }
}
