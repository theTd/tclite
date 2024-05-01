package com.mineclay.tclite.ui.playerinv;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerInvButtonSlot {
    private final int slot;
    private final boolean hotbar;

    public int getAbsoluteSlot() {
        return hotbar ? slot : slot + 9;
    }
}
