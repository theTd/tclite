package com.mineclay.tclite.ui;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractGUI implements InventoryHolder {
    private final Player player;
    final Map<Integer, AbstractButton> buttons = new HashMap<>();
    boolean inited = false;
    boolean opening = false;
    Inventory inv;
    int windowId = -1;

    public AbstractGUI(Player player) {
        this.player = player;
    }

    public void onClose() {
    }

    public GUIResponse onClickInventory(int slot, InventoryAction action) {
        return GUIResponse.NOTHING;
    }

    public void open() {
        UIService.inst().open(this);
    }

    public void close() {
        UIService.inst().close(this);
    }

    public void init() {
        UIService.inst().init(this);
    }

    public Player getPlayer() {
        return player;
    }

    public final int getWindowId() {
        return windowId;
    }

    public boolean isOpening() {
        return opening;
    }

    public abstract String getTitle();

    public abstract void initButtons();

    public void addButton(AbstractButton button, int slot) {
        Preconditions.checkArgument(slot >= 0, "slot cannot < 0");
        buttons.put(slot, button);
    }

    protected void addButton(AbstractButton button, int x, int y) {
        int slot = x + y * 9;
        addButton(button, slot);
    }

    public Map<Integer, AbstractButton> getButtons() {
        return new HashMap<>(buttons);
    }

    public AbstractButton getButtonBySlot(int slot) {
        return buttons.get(slot);
    }

    public List<Integer> getSlotsByButton(AbstractButton button) {
        return buttons.entrySet().stream().filter(en -> Objects.equals(button, en.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public <T extends AbstractButton> Map<Integer, T> getButtons(Class<T> clz) {
        Map<Integer, T> map = new HashMap<>();
        //noinspection unchecked
        buttons.entrySet().stream().filter(en -> clz.isInstance(en.getValue())).forEach(en -> map.put(en.getKey(), (T) en.getValue()));
        return map;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
