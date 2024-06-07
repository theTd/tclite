package com.mineclay.tclite.ui.numberpanelgui;

import com.mineclay.tclite.XMaterial;
import com.mineclay.tclite.ui.AbstractButton;
import com.mineclay.tclite.ui.GUIResponse;
import com.mineclay.tclite.ui.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class NumberButton extends AbstractButton {

    private final int num;

    public NumberButton(NumberPanelGUI gui, int number) {
        super(gui);
        this.num = number;
    }

    @Override
    public GUIResponse onClick(InventoryAction action) {
        NumberPanelGUI parent = getGUI();
        parent.append(num);
        return GUIResponse.REFRESH_GUI;
    }

    @Override
    public void init() {
        HashMap<String, String> map = new HashMap<>();
        map.put("[number]", this.num + "");

        ItemStack item = XMaterial.CYAN_STAINED_GLASS_PANE.parseItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "[number]");
        item.setItemMeta(meta);

        item = InventoryUtils.replaceAllInItemStack(item, map);
        item.setAmount(num == 0 ? 1 : num);
        setItem(item);
    }

    @Override
    public NumberPanelGUI getGUI() {
        return (NumberPanelGUI) super.getGUI();
    }
}
