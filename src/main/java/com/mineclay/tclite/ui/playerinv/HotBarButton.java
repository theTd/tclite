package com.mineclay.tclite.ui.playerinv;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class HotBarButton extends PlayerInvButton {
    private ItemStack hotbarItem = new ItemStack(Material.AIR);

    public void onFocus() {
    }

    public void onUnFocus() {
    }

    public void onDrop() {
    }

    public void setHotbarItem(ItemStack hotbarItem) {
        this.hotbarItem = hotbarItem;
    }

    public ItemStack getHotbarItem() {
        return hotbarItem;
    }
}
