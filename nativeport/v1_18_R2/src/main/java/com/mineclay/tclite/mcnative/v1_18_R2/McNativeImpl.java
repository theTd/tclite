package com.mineclay.tclite.mcnative.v1_18_R2;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.mineclay.tclite.AsyncTabCompleteEventSocket;
import com.mineclay.tclite.mcnative.McNative;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class McNativeImpl implements McNative {
    public void sendTitle0(Player player, String jsonTitle, String jsonSubtitle, int fadeIn, int keep, int fadeOut) {
        player.showTitle(Title.title(GsonComponentSerializer.gson().deserialize(jsonTitle),
                        GsonComponentSerializer.gson().deserialize(jsonSubtitle),
                        Title.Times.times(Duration.ofMillis(fadeIn * 50L),
                                Duration.ofMillis(keep * 50L),
                                Duration.ofMillis(fadeOut * 50L))
                )
        );
    }

    public void sendTitle0(Player player, String json) {
        player.sendTitlePart(TitlePart.TITLE, GsonComponentSerializer.gson().deserialize(json));
    }

    public void sendSubtitle0(Player player, String json) {
        player.sendTitlePart(TitlePart.SUBTITLE, GsonComponentSerializer.gson().deserialize(json));
    }

    public void clearTitle0(Player player) {
        player.clearTitle();
    }

    public void resetTitle0(Player player) {
        player.resetTitle();
    }

    public void setTimes0(Player player, int fadeIn, int keep, int fadeOut) {
        player.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(keep * 50L), Duration.ofMillis(fadeOut * 50L)));
    }

    public int getStateId0(Player player) {
        return ((CraftPlayer) player).getHandle().containerMenu.getStateId();
    }

    public int getActiveWindowId0(Player player) {
        return ((CraftPlayer) player).getHandle().containerMenu.containerId;
    }

    @Override
    public void sendSlotChange0(int windowId, int slot, ItemStack item, Player p) {
        if (item == null) item = new ItemStack(Material.AIR);

        PacketContainer packet;
        packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(0, windowId);
        packet.getIntegers().write(1, getStateId0(p));
        packet.getIntegers().write(2, slot);
        packet.getItemModifier().write(0, item);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public @Nullable AsyncTabCompleteEventSocket adaptAsyncTabCompleteEvent0(@NotNull Event event) {
        if (event instanceof AsyncTabCompleteEvent e) {
            return new AsyncTabCompleteEventSocket() {
                @Override
                public @NotNull CommandSender getSender() {
                    return e.getSender();
                }

                @Override
                public @NotNull String getBuffer() {
                    return e.getBuffer();
                }

                @Override
                public boolean isCommand() {
                    return e.isCommand();
                }

                @Override
                public @Nullable Location getLocation() {
                    return e.getLocation();
                }

                @Override
                public @NotNull List<String> getCompletions() {
                    return e.getCompletions();
                }

                @Override
                public void setCompletions(@NotNull List<String> completions) {
                    e.setCompletions(completions);
                }

                @Override
                public boolean isHandled() {
                    return e.isHandled();
                }

                @Override
                public void setHandled(boolean handled) {
                    e.setHandled(handled);
                }

                @Override
                public boolean isCancelled() {
                    return e.isCancelled();
                }

                @Override
                public void setCancelled(boolean cancelled) {
                    e.setCancelled(cancelled);
                }
            };
        } else return null;
    }

    @Override
    public CommandMap getCommandMap0() {
        return Bukkit.getServer().getCommandMap();
    }

    @Override
    public void syncCommands0() {
        ((CraftServer) Bukkit.getServer()).syncCommands();
    }
}
