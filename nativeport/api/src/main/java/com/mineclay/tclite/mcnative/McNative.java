package com.mineclay.tclite.mcnative;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;
import java.util.logging.Logger;

public class McNative {
    private static McNativePort impl = new McNativePort() {
        @Override
        public void sendTitle(Player player, String title, String subtitle, int fadeIn, int keep, int fadeOut) {
        }

        @Override
        public void sendTitle(Player player, String title) {
        }

        @Override
        public void sendSubtitle(Player player, String subtitle) {
        }

        @Override
        public void clearTitle(Player player) {
        }

        @Override
        public void resetTitle(Player player) {
        }

        @Override
        public void setTimes(Player player, int fadeIn, int keep, int fadeOut) {
        }

        @Override
        public int getStateId(Player player) {
            return -1;
        }

        @Override
        public int getActiveWindowId(Player player) {
            return -1;
        }

        @Override
        public void sendSlotChange(int windowId, int slot, ItemStack item, Player p) {
        }
    };

    static {
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String version = pkg.substring(pkg.lastIndexOf('.') + 1);
        try {
            Class<?> clazz = Class.forName(McNativePort.class.getName() + "_" + version);
            impl = (McNativePort) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Logger.getLogger(McNative.class.getName()).log(Level.SEVERE, "Failed to load native port for version " + version, e);
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int keep, int fadeOut) {
        impl.sendTitle(player, title, subtitle, fadeIn, keep, fadeOut);
    }

    public static void sendTitle(Player player, String title) {
        impl.sendTitle(player, title);
    }

    public static void sendSubtitle(Player player, String subtitle) {
        impl.sendSubtitle(player, subtitle);
    }

    public static void clearTitle(Player player) {
        impl.clearTitle(player);
    }

    public static void resetTitle(Player player) {
        impl.resetTitle(player);
    }

    public static void setTimes(Player player, int fadeIn, int keep, int fadeOut) {
        impl.setTimes(player, fadeIn, keep, fadeOut);
    }

    public static int getStateId(Player player) {
        return impl.getStateId(player);
    }

    public static int getActiveWindowId(Player player) {
        return impl.getActiveWindowId(player);
    }

    public static void sendSlotChange(int windowId, int slot, ItemStack item, Player p) {
        impl.sendSlotChange(windowId, slot, item, p);
    }
}
