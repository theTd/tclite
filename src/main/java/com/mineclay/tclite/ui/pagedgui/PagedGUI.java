package com.mineclay.tclite.ui.pagedgui;

import com.mineclay.tclite.ui.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class PagedGUI<T> extends AbstractGUI {
    protected int page;
    protected List<T> elements;

    public PagedGUI(Player p, int page) {
        super(p);
        this.page = page;
    }

    public final void initButtons() {
        elements = getElements();
        if (!elements.isEmpty()) elements.sort(getComparator());

        int maxPage = getMaxPage();

        if (this.page > maxPage && maxPage != 0) {
            this.page = maxPage;
        }
        Iterator<T> ite = elements.iterator();
        if (page > 1) for (int i = 0; i < 36 * (page - 1); i++) ite.next();
        for (int i = 0; i < 36; i++) {
            if (!ite.hasNext()) {
                break;
            }
            T element = ite.next();
            PagedGUIElementButton<T> button = createElementButton(element);
            addButton(button, i);
        }
        addButton(new PreviousPageButton(), 36);
        addButton(new NextPageButton(), 44);
        AbstractButton blankButton = new SimpleButton(this, InventoryUtils.genBlankButton());
        for (int i = 37; i < 44; i++) {
            addButton(blankButton, i);
        }
        putAdditionalButtons();
    }

    protected void putAdditionalButtons() {
    }

    protected abstract List<T> getElements();

    protected abstract Comparator<? super T> getComparator();

    protected void pageChange(int page) {
        this.page = page;
        init();
        open();
    }

    protected abstract PagedGUIElementButton<T> createElementButton(T element);

    protected int getMaxPage() {
        return new BigDecimal(elements.size() / 36D).setScale(0, RoundingMode.UP).intValue();
    }

    public int getPage() {
        return page;
    }

    private class PreviousPageButton extends AbstractButton {
        PreviousPageButton() {
            super(PagedGUI.this);
        }

        @Override
        public GUIResponse onClick(InventoryAction action) {
            if (action == InventoryAction.PICKUP_ALL) {
                if (page <= 1) {
                    return GUIResponse.NOTHING;
                }
                pageChange(page - 1);
                return GUIResponse.REFRESH_GUI;
            } else {
                return GUIResponse.NOTHING;
            }
        }

        @Override
        public void init() {
        }

        @Override
        public void refresh() {
            if (page <= 1) {
                setItem(InventoryUtils.genBlankButton());
            } else {
                ItemStack display = new ItemStack(Material.DIODE);
                ItemMeta meta = display.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "上一页");
                display.setItemMeta(meta);
                setItem(display);
            }
        }
    }

    private class NextPageButton extends AbstractButton {
        NextPageButton() {
            super(PagedGUI.this);
        }

        @Override
        public GUIResponse onClick(InventoryAction action) {
            if (action == InventoryAction.PICKUP_ALL) {
                if (page >= getMaxPage()) {
                    return GUIResponse.NOTHING;
                }
                pageChange(page + 1);
                return GUIResponse.REFRESH_GUI;
            }
            return GUIResponse.NOTHING;
        }

        @Override
        public void init() {
        }

        @Override
        public void refresh() {
            if (page >= getMaxPage()) {
                setItem(InventoryUtils.genBlankButton());
            } else {
                ItemStack item = new ItemStack(Material.REDSTONE_COMPARATOR);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "下一页");
                item.setItemMeta(meta);
                setItem(item);
            }
        }
    }

}
