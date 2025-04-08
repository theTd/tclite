package com.mineclay.tclite.mcnative;

import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

class Implementation {
    final static McNative NOP = new McNative() {
        @Override
        public void sendTitle0(Player player, String title, String subtitle, int fadeIn, int keep, int fadeOut) {
        }

        @Override
        public void sendTitle0(Player player, String title) {
        }

        @Override
        public void sendSubtitle0(Player player, String subtitle) {
        }

        @Override
        public void clearTitle0(Player player) {
        }

        @Override
        public void resetTitle0(Player player) {
        }

        @Override
        public void setTimes0(Player player, int fadeIn, int keep, int fadeOut) {
        }

        @Override
        public int getStateId0(Player player) {
            return -1;
        }

        @Override
        public int getActiveWindowId0(Player player) {
            return -1;
        }

        @Override
        public void sendSlotChange0(int windowId, int slot, ItemStack item, Player p) {
        }

        @Override
        public CommandMap getCommandMap0() {
            return null;
        }

        @Override
        public void syncCommands0() {
        }
    };

    final static McNative IMPL;

    static {
        MinecraftVersion currentVersion = MinecraftVersion.getCurrentVersion();
        ClassLoader cl = Implementation.class.getClassLoader();
        Class<?> preciseMatch = null;
        for (Class<?> aClass : new Reflections(new ConfigurationBuilder()
                .addUrls(((URLClassLoader) cl).getURLs())
        ).get(Scanners.TypesAnnotated.of(VersionMark.class).asClass(cl))) {
            VersionMark versionMark = aClass.getAnnotation(VersionMark.class);
            if (versionMark.major() == currentVersion.getMajor() &&
                    versionMark.minor() == currentVersion.getMinor() &&
                    versionMark.build() == currentVersion.getBuild()
            ) {
                preciseMatch = aClass;
            }
        }

        Logger logger = JavaPlugin.getProvidingPlugin(Implementation.class).getLogger();
        McNative impl = NOP;

        if (preciseMatch != null) {
            logger.info("Using precise match tclite implementation class " + preciseMatch.getName());
            try {
                impl = preciseMatch.asSubclass(McNative.class).newInstance();
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "failed loading McNative implementation " + preciseMatch, e);
            }
        } else {
            String pkg = Bukkit.getServer().getClass().getPackage().getName();
            String version = pkg.substring(pkg.lastIndexOf('.') + 1);
            String implClassName = McNative.class.getPackage().getName() + "." + version + ".McNativeImpl";
            try {
                impl = Class.forName(implClassName).asSubclass(McNative.class).newInstance();
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "failed loading McNative implementation " + implClassName, e);
            }
        }

        IMPL = impl;
    }
}
