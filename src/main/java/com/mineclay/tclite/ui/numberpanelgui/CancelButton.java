package com.mineclay.tclite.ui.numberpanelgui;

import com.mineclay.tclite.ui.AbstractButton;
import com.mineclay.tclite.ui.GUIResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class CancelButton extends AbstractButton {
    public CancelButton(NumberPanelGUI gui) {
        super(gui);
    }

    @Override
    public GUIResponse onClick(InventoryAction action) {
        getGUI().closed();
        return GUIResponse.CLOSE;
    }

    @Override
    public void init() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
        item.setDurability((short) 14);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "取消");
        item.setItemMeta(meta);
        setItem(item);
    }

    @Override
    public void refresh() {
    }

    @Override
    public NumberPanelGUI getGUI() {
        return (NumberPanelGUI) super.getGUI();
    }
}
