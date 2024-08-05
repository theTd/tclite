package com.mineclay.tclite.mcnative.v1_12_R1;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.mineclay.tclite.mcnative.McNative;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.joor.Reflect.on;

public class McNativeImpl implements McNative {
    @Override
    public void sendTitle0(Player player, String jsonTitle, String jsonSubtitle, int fadeIn, int keep, int fadeOut) {
        player.sendTitle(BaseComponent.toLegacyText(ComponentSerializer.parse(jsonTitle)),
                BaseComponent.toLegacyText(ComponentSerializer.parse(jsonSubtitle))
                , fadeIn, keep, fadeOut);
    }

    @Override
    public void sendTitle0(Player player, String json) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTitlePacket(EnumWrappers.TitleAction.TITLE, json));
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public void sendSubtitle0(Player player, String json) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTitlePacket(EnumWrappers.TitleAction.SUBTITLE, json));
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public void clearTitle0(Player player) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTitlePacket(EnumWrappers.TitleAction.CLEAR, null));
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public void resetTitle0(Player player) {
        PacketContainer packet = createTitlePacket(EnumWrappers.TitleAction.RESET, null);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public void setTimes0(Player player, int fadeIn, int keep, int fadeOut) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTitlePacket(EnumWrappers.TitleAction.TIMES, null, fadeIn, keep, fadeOut));
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public int getStateId0(Player player) {
        return -1;
    }

    @Override
    public int getActiveWindowId0(Player player) {
        Object activeContainer = on(player).call("getHandle").get("activeContainer");
        return on(activeContainer).get("windowId");
    }

    @Override
    public void sendSlotChange0(int windowId, int slot, ItemStack item, Player p) {
        if (item == null) item = new ItemStack(Material.AIR);

        PacketContainer packet;
        packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(0, windowId);
        packet.getIntegers().write(1, slot);
        packet.getItemModifier().write(0, item);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public CommandMap getCommandMap0() {
        return on(Bukkit.getServer()).call("getCommandMap").get();
    }

    @Override
    public void syncCommands0() {
    }

    private static PacketContainer createTitlePacket(EnumWrappers.TitleAction action, String json) {
        return createTitlePacket(action, json, -1, -1, -1);
    }

    private static PacketContainer createTitlePacket(EnumWrappers.TitleAction action, String raw, int fadeIn, int keep, int fadeOut) {
        PacketContainer packet;
        try {
            packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TITLE);
            packet.getEnumModifier(EnumWrappers.TitleAction.class, 0).write(0, action);
            if (raw != null)
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(raw));
            StructureModifier<Integer> t = packet.getIntegers();
            t.write(0, fadeIn);
            t.write(1, keep);
            t.write(2, fadeOut);
            return packet;
        } catch (IllegalArgumentException e) {
            switch (action) {
                case TITLE:
                    packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_TITLE_TEXT);
                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(raw));
                    break;
                case SUBTITLE:
                    packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SUBTITLE_TEXT);
                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(raw));
                    break;
                case TIMES:
                    packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_TITLES_ANIMATION);
                    packet.getIntegers().write(0, fadeIn);
                    packet.getIntegers().write(1, keep);
                    packet.getIntegers().write(2, fadeOut);
                    break;
                case CLEAR:
                    packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CLEAR_TITLES);
                    packet.getBooleans().write(0, false);
                    break;
                case RESET:
                    packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CLEAR_TITLES);
                    packet.getBooleans().write(0, true);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        return packet;
    }
}
