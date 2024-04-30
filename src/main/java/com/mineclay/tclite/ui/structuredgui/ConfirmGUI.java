package com.mineclay.tclite.ui.structuredgui;

import com.mineclay.tclite.ui.AbstractGUI;
import com.mineclay.tclite.ui.GUIResponse;
import com.mineclay.tclite.ui.SimpleButton;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class ConfirmGUI extends AbstractStructuredGUI {
    private final String title;
    private final List<String> yesLore;
    private final List<String> noLore;

    public ConfirmGUI(Player player, String title, List<String> yesLore, List<String> noLore) {
        super(player);
        this.title = title;
        this.yesLore = yesLore;
        this.noLore = noLore;
        setDoNotDrawSideBorder();
    }

    @Override
    protected void putContentButtons() {
        ItemStack icon = new ItemStack(Material.STAINED_GLASS_PANE);
        icon.setDurability((short) 5);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "确定");
        List<String> lore = new ArrayList<>();
        if (this.yesLore != null && !this.yesLore.isEmpty()) {
            lore.add("");
            lore.addAll(this.yesLore);
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
        addButton(new SimpleButton(this, icon, this::yes), 3);

        icon = new ItemStack(Material.STAINED_GLASS_PANE);
        icon.setDurability((short) 14);
        meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "取消");
        lore = new ArrayList<>();
        if (this.noLore != null && !this.noLore.isEmpty()) {
            lore.add("");
            lore.addAll(this.noLore);
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);

        addButton(new SimpleButton(this, icon, this::no), 7);
    }

    protected abstract AbstractGUI getParentGUI();

    protected abstract GUIResponse yes();

    protected abstract GUIResponse no();

    @Override
    public String getTitle() {
        return title == null ? "确定?" : title;
    }
}