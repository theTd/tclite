package com.mineclay.tclite.ui.pagedgui;

import com.mineclay.tclite.ui.AbstractButton;
import com.mineclay.tclite.ui.AbstractGUI;

public abstract class PagedGUIElementButton<T> extends AbstractButton {
    private final T element;

    public PagedGUIElementButton(AbstractGUI gui, T element) {
        super(gui);
        this.element = element;
    }

    public T getElement() {
        return element;
    }
}
