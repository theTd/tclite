package com.mineclay.tclite.ui.structuredgui;

import com.google.common.base.Preconditions;
import com.mineclay.tclite.XMaterial;
import com.mineclay.tclite.ui.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class AbstractStructuredGUI extends AbstractGUI {
    public AbstractStructuredGUI(Player p) {
        super(p);
    }

    public AbstractStructuredGUI(AbstractStructuredGUI parent) {
        super(parent.getPlayer());
    }

    @Override
    public void initButtons() {

        AbstractGUI parentGUI = getParentGUI();
        if (parentGUI != null) {
            ItemStack backDisplay = XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem();
            ItemMeta meta = backDisplay.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "返回");
            backDisplay.setItemMeta(meta);
            super.addButton(new SimpleButton(this, backDisplay, () -> {
                parentGUI.init();
                return new GUIResponse(GUIResponse.Action.OPEN, parentGUI);
            }), 0);
        }

        drawUpperBorder();
        putContentButtons();
        drawSideBorder();
        drawBottomBorder();
        putAdditionalButton();

    }

    @Override
    public void addButton(AbstractButton button, int slot) {
        super.addButton(button, getContentSlot(slot, !doNotDrawSideBorder));
    }

    @Override
    protected void addButton(AbstractButton button, int x, int y) {
        int slot = x + y * 9;
        addButton(button, slot);
    }

    public void addButtonAbsolutely(AbstractButton button, int slot) {
        super.addButton(button, slot);
    }

    protected abstract void putContentButtons();

    protected abstract AbstractGUI getParentGUI();

    protected void drawUpperBorder() {
        for (int i = 0; i < 8; i++) {
            if (getButtonBySlot(i) == null) {
                super.addButton(new SimpleButton(this, InventoryUtils.genBlankButton()), i);
            }
        }
        ItemStack closeDisplay = XMaterial.RED_STAINED_GLASS_PANE.parseItem();
        ItemMeta meta = closeDisplay.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "关闭");
        closeDisplay.setItemMeta(meta);
        super.addButton(new SimpleButton(this, closeDisplay, () -> GUIResponse.CLOSE), 8);
    }

    private boolean doNotDrawSideBorder = false;

    protected void setDoNotDrawSideBorder() {
        doNotDrawSideBorder = true;
    }

    protected void drawSideBorder() {
        if (doNotDrawSideBorder) {
            return;
        }
        int row = getRow() + 1;

        if (forceLine != -1) {
            if (forceLine + 1 > row) {
                row = forceLine + 1;
            }
        }
        for (int curRow = 1; curRow <= row; curRow++) {
            int start = (curRow - 1) * 9;
            int end = start + 8;

            for (Integer i : new int[]{start, end}) {
                if (getButtonBySlot(i) == null) {
                    super.addButton(new SimpleButton(this, InventoryUtils.genBlankButton()), i);
                }
            }
        }

    }

    protected void drawBottomBorder() {
        int row = getRow() + 1;
        if (forceLine != -1) {
            if (forceLine + 1 > row) {
                row = forceLine + 1;
            }
        }
        for (int i = row * 9; i < (row + 1) * 9; i++) {
            if (getButtonBySlot(i) == null) {
                super.addButton(new SimpleButton(this, InventoryUtils.genBlankButton()), i);
            }
        }
    }

    protected void putAdditionalButton() {
    }

    private int forceLine = -1;

    protected void forceSetLine(int line) {
        Preconditions.checkArgument(line >= 1 && line <= 4, "line must be between 1 and 4");
        forceLine = line;
    }

    @Override
    public void onClose() {
    }

    public int getRow() {
        int max = 0;

        for (Integer i : getButtons().keySet()) {
            if (i > max) {
                max = i;
            }
        }

        return new BigDecimal(max / 9).setScale(0, RoundingMode.UP).intValue();
    }

    public static int getContentSlot(int slot, boolean side) {
        int cur = 0;
        for (int i = 0; i < 54; i++) {
            if (isBorderSlot(i, side)) {
                continue;
            }
            if (++cur == slot) {
                return i;
            }
        }
        throw new RuntimeException("could not get valid content slot");
    }

    public static int getContentSlot(int x, int y, boolean side) {
        if (!side) {
            x -= 1;
        }
        int slot = y * 9 + x;
        if (isBorderSlot(slot, side)) {
            throw new RuntimeException("could not get valid content slot");
        }
        return slot;
    }

    private static boolean isBorderSlot(int slot, boolean side) {
        boolean pre = slot < 9 || slot > 44;
        return side ? pre || (slot + 1) % 9 == 0 || slot % 9 == 0 : pre;
    }

}