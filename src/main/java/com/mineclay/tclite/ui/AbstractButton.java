package com.mineclay.tclite.ui;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractButton {
    private final AbstractGUI gui;
    ItemStack item;

    public AbstractButton(AbstractGUI gui) {
        this.gui = gui;
    }

    public abstract GUIResponse onClick(InventoryAction action);

    public abstract void init();

    public void refresh() {
    }

    protected ItemStack getItem() {
        return item == null ? null : item.clone();
    }

    protected void setItem(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            item = MinecraftReflection.getBukkitItemStack(item);
            NbtCompound itemTag = (NbtCompound) NbtFactory.fromItemTag(item);
            itemTag.put("tcbaselib_inv_btn", (byte) 1);
            NbtFactory.setItemTag(item, itemTag);
        }
        this.item = item;
    }

    public AbstractGUI getGUI() {
        return gui;
    }

    protected void update() {
        UIService.inst().updateButton(this);
    }
}
