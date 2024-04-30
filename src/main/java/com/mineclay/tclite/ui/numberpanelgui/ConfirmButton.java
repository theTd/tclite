package com.mineclay.tclite.ui.numberpanelgui;

import com.mineclay.tclite.ui.AbstractButton;
import com.mineclay.tclite.ui.GUIResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class ConfirmButton extends AbstractButton {
    public ConfirmButton(NumberPanelGUI gui) {
        super(gui);
    }

    @Override
    public GUIResponse onClick(InventoryAction action) {
        NumberPanelGUI parent = getGUI();
        String number = parent.getNumber();

        if (number.startsWith(".")) {
            number = "0" + number;
        }

        if (number.isEmpty()) {
            return GUIResponse.NOTHING;
        }

        if (number.endsWith(".")) {
            return GUIResponse.NOTHING;
        }

        try {
            getGUI().receive(number.isEmpty() ? null : Double.parseDouble(number));
        } catch (Exception ignored) {
            getGUI().closed();
            return GUIResponse.CLOSE;
        }
        return GUIResponse.CLOSE;
    }

    @Override
    public void init() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
        item.setDurability((short) 5);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "确认");
        item.setItemMeta(meta);
        setItem(item);
    }

    @Override
    public NumberPanelGUI getGUI() {
        return (NumberPanelGUI) super.getGUI();
    }
}
