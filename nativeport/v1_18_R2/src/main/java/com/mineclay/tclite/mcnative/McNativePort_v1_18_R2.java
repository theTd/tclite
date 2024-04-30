package com.mineclay.tclite.mcnative;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class McNativePort_v1_18_R2 implements McNativePort {

    public void sendTitle(Player player, String jsonTitle, String jsonSubtitle, int fadeIn, int keep, int fadeOut) {
        player.showTitle(Title.title(GsonComponentSerializer.gson().deserialize(jsonTitle),
                        GsonComponentSerializer.gson().deserialize(jsonSubtitle),
                        Title.Times.times(Duration.ofMillis(fadeIn * 50L),
                                Duration.ofMillis(keep * 50L),
                                Duration.ofMillis(fadeOut * 50L))
                )
        );
    }

    public void sendTitle(Player player, String json) {
        player.sendTitlePart(TitlePart.TITLE, GsonComponentSerializer.gson().deserialize(json));
    }

    public void sendSubtitle(Player player, String json) {
        player.sendTitlePart(TitlePart.SUBTITLE, GsonComponentSerializer.gson().deserialize(json));
    }

    public void clearTitle(Player player) {
        player.clearTitle();
    }

    public void resetTitle(Player player) {
        player.resetTitle();
    }

    public void setTimes(Player player, int fadeIn, int keep, int fadeOut) {
        player.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(keep * 50L), Duration.ofMillis(fadeOut * 50L)));
    }

    public int getStateId(Player player) {
        return ((CraftPlayer) player).getHandle().containerMenu.getStateId();
//        return ((CraftPlayer) player).getHandle().bV.j();
    }

    public int getActiveWindowId(Player player) {
        return ((CraftPlayer) player).getHandle().containerMenu.containerId;
//        return ((CraftPlayer) player).getHandle().bV.j;
    }

    @Override
    public void sendSlotChange(int windowId, int slot, ItemStack item, Player p) {
        if (item == null) item = new ItemStack(Material.AIR);

        PacketContainer packet;
        packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(0, windowId);
        packet.getIntegers().write(1, getStateId(p));
        packet.getIntegers().write(2, slot);
        packet.getItemModifier().write(0, item);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "sending packet", e);
        }
    }
}
