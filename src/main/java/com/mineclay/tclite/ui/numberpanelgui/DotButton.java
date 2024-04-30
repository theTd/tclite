package com.mineclay.tclite.ui.numberpanelgui;

import com.mineclay.tclite.ui.AbstractButton;
import com.mineclay.tclite.ui.GUIResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DotButton extends AbstractButton {
    public DotButton(NumberPanelGUI gui) {
        super(gui);
    }

    @Override
    public GUIResponse onClick(InventoryAction action) {

        NumberPanelGUI parent = getGUI();

        String before = parent.getNumber();
        parent.appendDot();
        if (parent.getNumber().equals(before)) {
            return GUIResponse.NOTHING;
        }
        return GUIResponse.REFRESH_GUI;
    }

    @Override
    public void init() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
        item.setDurability((short) 7);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + ".");
        item.setItemMeta(meta);
        setItem(item);
    }

    @Override
    public NumberPanelGUI getGUI() {
        return (NumberPanelGUI) super.getGUI();
    }
}
