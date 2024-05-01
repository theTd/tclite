package com.mineclay.tclite.ui.playerinv;

import com.mineclay.tclite.PlayerScanner;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

class ValidateScanner extends PlayerScanner {
    private final PlayerInvUiService manager;

    public ValidateScanner(PlayerInvUiService manager) {
        super(100);
        this.manager = manager;
    }

    @Override
    public void handle(Player player) {
        Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = manager.objectMap.get(player.getUniqueId());
        if (buttonMap == null) return;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (i >= 37) return;

            ItemStack actualItem = player.getInventory().getItem(i);
            boolean itemNull = actualItem == null || actualItem.getType() == Material.AIR;
            PlayerInvButtonSlot slot;
            if (i < 9) {
                slot = new PlayerInvButtonSlot(i, true);
            } else {
                slot = new PlayerInvButtonSlot(i - 9, false);
            }

            PlayerInvButton button = buttonMap.get(slot);

            /*if (itemNull && button != null) {
                button.update();
            } else */
            if (!itemNull && button == null && manager.isInvButton(actualItem)) {
                player.getInventory().setItem(i, null);
            }
        }
    }
}
