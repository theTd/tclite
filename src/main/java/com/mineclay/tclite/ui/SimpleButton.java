package com.mineclay.tclite.ui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

public class SimpleButton extends AbstractButton {
    private final ClickHandler handler;
    private final ItemStack display;

    public SimpleButton(AbstractGUI gui, ItemStack display, ClickHandler handler) {
        super(gui);
        this.display = display;
        this.handler = handler;
    }

    public SimpleButton(AbstractGUI gui, ItemStack display) {
        this(gui, display, null);
    }

    public SimpleButton(AbstractGUI gui) {
        this(gui, new ItemStack(Material.AIR));
    }

    @Override
    public GUIResponse onClick(InventoryAction action) {
        if (action != InventoryAction.PICKUP_ALL) return GUIResponse.NOTHING;
        if (handler == null) return GUIResponse.NOTHING;
        return handler.onClick();
    }

    @Override
    public void init() {
        setItem(display);
    }

    public interface ClickHandler {
        GUIResponse onClick();
    }
}
