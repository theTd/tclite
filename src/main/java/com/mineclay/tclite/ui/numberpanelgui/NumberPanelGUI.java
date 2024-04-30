package com.mineclay.tclite.ui.numberpanelgui;

import com.mineclay.tclite.ui.AbstractGUI;
import org.bukkit.entity.Player;

public abstract class NumberPanelGUI extends AbstractGUI {
    private final String title;
    private String input = "";

    public NumberPanelGUI(Player p, String title) {
        super(p);
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title.replace("[current]", input);
    }

    @Override
    public void initButtons() {
        addButton(new ConfirmButton(this), 0);
        addButton(new CancelButton(this), 1);

        int startX = 5;
        int startY = 1;
        addButton(new NumberButton(this, 1), startX, startY);
        addButton(new NumberButton(this, 2), startX + 1, startY);
        addButton(new NumberButton(this, 3), startX + 2, startY);
        addButton(new NumberButton(this, 4), startX, startY + 1);
        addButton(new NumberButton(this, 5), startX + 1, startY + 1);
        addButton(new NumberButton(this, 6), startX + 2, startY + 1);
        addButton(new NumberButton(this, 7), startX, startY + 2);
        addButton(new NumberButton(this, 8), startX + 1, startY + 2);
        addButton(new NumberButton(this, 9), startX + 2, startY + 2);
        if (allowDecimal()) addButton(new DotButton(this), startX, startY + 3);
        addButton(new NumberButton(this, 0), startX + 1, startY + 3);
        addButton(new BackSpaceButton(this), startX + 2, startY + 3);
    }

    public String getNumber() {
        return input;
    }

    public void appendDot() {

        if (input.contains(".")) {
            return;
        }

        input = input + '.';

    }

    public void append(int i) {
        if (!(i < 10 && i > -1)) {
            return;
        }

        input = input + i;
    }

    public void setNumber(String num) {
        this.input = num;
    }

    public void back() {
        if (input.isEmpty()) {
            return;
        }
        input = input.substring(0, input.length() - 1);
    }

    public boolean allowDecimal() {
        return false;
    }

    public abstract void receive(Double result);

    public abstract void closed();
}
