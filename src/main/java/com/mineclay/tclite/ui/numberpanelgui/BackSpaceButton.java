package com.mineclay.tclite.ui.numberpanelgui;

import com.mineclay.tclite.ui.AbstractButton;
import com.mineclay.tclite.ui.GUIResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

class BackSpaceButton extends AbstractButton {
    public BackSpaceButton(NumberPanelGUI gui) {
        super(gui);
    }

    @Override
    public GUIResponse onClick(InventoryAction action) {
        NumberPanelGUI parent = getGUI();

        String before = parent.getNumber();
        parent.back();
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
        meta.setDisplayName(ChatColor.RESET + "<-");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "修正");
        meta.setLore(lore);
        item.setItemMeta(meta);
        setItem(item);
    }

    @Override
    public NumberPanelGUI getGUI() {
        return (NumberPanelGUI) super.getGUI();
    }
}
