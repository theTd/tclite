package com.mineclay.tclite.mcnative;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class McNativePort_v1_12_R2 implements McNativePort {
    @Override
    public void sendTitle(Player player, String jsonTitle, String jsonSubtitle, int fadeIn, int keep, int fadeOut) {
        player.sendTitle(BaseComponent.toLegacyText(ComponentSerializer.parse(jsonTitle)),
                BaseComponent.toLegacyText(ComponentSerializer.parse(jsonSubtitle))
                , fadeIn, keep, fadeOut);
    }

    @Override
    public void sendTitle(Player player, String json) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTitlePacket(EnumWrappers.TitleAction.TITLE, json));
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public void sendSubtitle(Player player, String json) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTitlePacket(EnumWrappers.TitleAction.SUBTITLE, json));
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public void clearTitle(Player player) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTitlePacket(EnumWrappers.TitleAction.CLEAR, null));
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public void resetTitle(Player player) {
        PacketContainer packet = createTitlePacket(EnumWrappers.TitleAction.RESET, null);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public void setTimes(Player player, int fadeIn, int keep, int fadeOut) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, createTitlePacket(EnumWrappers.TitleAction.TIMES, null, fadeIn, keep, fadeOut));
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }

    @Override
    public int getStateId(Player player) {
        return -1;
    }

    @Override
    public int getActiveWindowId(Player player) {
        initReflection(player);
        try {
            Object activeContainer = activeContainerField.get(getHandleMethod.invoke(player));
            if (activeContainer == null) return -1;
            return (int) windowIdField.get(activeContainer);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendSlotChange(int windowId, int slot, ItemStack item, Player p) {
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

    private static Method getHandleMethod;
    private static Field activeContainerField;
    private static Field windowIdField;

    private static void initReflection(Player playerInstance) {
        if (getHandleMethod != null && activeContainerField != null && windowIdField != null) return;
        try {
            getHandleMethod = playerInstance.getClass().getDeclaredMethod("getHandle");
            Object nmsPlayer = getHandleMethod.invoke(playerInstance);
            activeContainerField = nmsPlayer.getClass().getDeclaredField("activeContainer");
            windowIdField = Class.forName(activeContainerField.getGenericType().getTypeName()).getDeclaredField("windowId");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException |
                 ClassNotFoundException e) {
            throw new RuntimeException("Reflection Initialize Failure", e);
        }
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
