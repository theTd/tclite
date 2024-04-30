package com.mineclay.tclite;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ColorUtil {
    public enum GeneralColor {
        WHITE(((short) 0), ChatColor.WHITE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVhNzcwZTdlNDRiM2ExZTZjM2I4M2E5N2ZmNjk5N2IxZjViMjY1NTBlOWQ3YWE1ZDUwMjFhMGMyYjZlZSJ9fX0="),
        ORANGE(((short) 1), ChatColor.GOLD, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVhNTkwYjY4MTU4OWZiOWIwZTg2NjRlZTk0NWI0MWViMzg1MWZhZjY2YWFmNDg1MjVmYmExNjljMzQyNzAifX19"),
        LIGHT_PURPLE(((short) 2), ChatColor.LIGHT_PURPLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjYxN2QxNzEzMmQ5NWIwYjNmOWYyMDdiYTI5NGNlOTNiZDdhMjEwM2QyZmYwNjI5NjYxNmI0YWQxNzhjYyJ9fX0="),
        AQUA(((short) 3), ChatColor.AQUA, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDg5Y2U4OTUyNmZjMTI2MjQ2NzhmMzA1NDkzYWE2NWRhOGExYjM2MDU0NmE1MDVkMTE4ZWIxZmFkNzc1In19fQ=="),
        YELLOW(((short) 4), ChatColor.YELLOW, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRjNDE0MWMxZWRmM2Y3ZTQxMjM2YmQ2NThjNWJjN2I1YWE3YWJmN2UyYTg1MmI2NDcyNTg4MThhY2Q3MGQ4In19fQ=="),
        GREEN(((short) 5), ChatColor.GREEN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I3ODg5M2U1M2ZlN2U4NDQ3YmMwNzY2ODIyZjg1ZmUzNmZmYTkxNWFiZGJmNmNjOTc4MjY2YTA3ZDNlYWMifX19"),
        PINK(((short) 6), ChatColor.LIGHT_PURPLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA3MzI2ZDMxODU4ZWE1N2U3YmM1NWYzZTc1ZTZjODViMzRmZjRiZmQyODA4OGY5NGYxMWViOGUwZDFjZiJ9fX0="),
        GRAY(((short) 7), ChatColor.GRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQ3YWJjNDM2ODI5MzA3MGRjYjE2ZTRmN2ZlMGMyMWM3MzE3MTIxOTllYjI3MmYyZmRiN2E2ZmU2OTZjYmYzIn19fQ=="),
        LIGHT_GRAY(((short) 8), ChatColor.GRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFjNDVhNTk1NTAxNDNhNDRlZDRlODdjZTI5NTVlNGExM2U5NGNkZmQ0YzY0ZGVlODgxZGZiNDhkZDkyZSJ9fX0="),
        DARK_AQUA(((short) 9), ChatColor.DARK_AQUA, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmMzNWY3MzA5OGQ1ZjViNDkyYWY4N2Q5YzU3ZmQ4ZGFhMWM4MmNmN2Y5YTdlYjljMzg0OTgxYmQ3NmRkOSJ9fX0="),
        PURPLE(((short) 10), ChatColor.DARK_PURPLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTkzNTJiY2FiZmMyN2VkYjQ0Y2ViNTFiMDQ3ODY1NDJmMjZhMjk5YTA1Mjk0NzUzNDYxODZlZTk0NzM4ZiJ9fX0="),
        BLUE(((short) 11), ChatColor.BLUE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjgzOWUzODFkOWZlZGFiNmY4YjU5Mzk2YTI3NjQyMzhkY2ViMmY3ZWVhODU2ZGM2ZmM0NDc2N2RhMzgyZjEifX19"),
        BROWN(((short) 12), ChatColor.DARK_GRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkNTk4M2RjYWJjOTMxYWI2YTQ5ZDJmYjg4NzllYmM1Mjk1Y2I1YmEyZjI3OGUzYzhhM2RhN2JjOGI0NzgifX19"),
        DARK_GREEN(((short) 13), ChatColor.DARK_GREEN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg1ZmM5N2M3ZGYyNGE2YWE5YzBhYzg5ZmNiMjJiODE3MDBmNTk5ZjQ1YzMyYzdlMzE3OGI0NDQxNzJkZiJ9fX0="),
        RED(((short) 14), ChatColor.DARK_RED, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGJhYzc3NTIwYjllZWU2NTA2OGVmMWNkOGFiZWFkYjAxM2I0ZGUzOTUzZmQyOWFjNjhlOTBlNDg2NjIyNyJ9fX0="),
        BLACK(((short) 15), ChatColor.BLACK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWRkZWJiYjA2MmY2YTM4NWE5MWNhMDVmMThmNWMwYWNiZTMzZTJkMDZlZTllNzQxNmNlZjZlZTQzZGZlMmZiIn19fQ==");

        private final short metaId;
        private final ChatColor chatColor;
        private final String texture;

        GeneralColor(short metaId, ChatColor chatColor, String texture) {
            this.metaId = metaId;
            this.chatColor = chatColor;
            this.texture = texture;
        }

        public short getMetaId() {
            return metaId;
        }

        public ChatColor getChatColor() {
            return chatColor;
        }

        public ItemStack getHead() {
            return fromWhitelistedUrl(texture);
        }

        public static GeneralColor forName(String name) {
            for (GeneralColor c : GeneralColor.values()) {
                if (c.name().equalsIgnoreCase(name.replace(" ", "_"))) return c;
            }
            return null;
        }
    }

    private static ItemStack fromWhitelistedUrl(String url) {
        ItemStack itemStack = MinecraftReflection.getBukkitItemStack(new ItemStack(Material.SKULL_ITEM, 1, (short) 3));
        NbtFactory.setItemTag(itemStack, createCompound(url));
        return itemStack;
    }

    private static NbtCompound createCompound(String decodedImage) {
        NbtCompound compound = NbtFactory.ofCompound("");
        NbtCompound skullOwner = NbtFactory.ofCompound("");

        skullOwner.put("Id", UUID.nameUUIDFromBytes(UUID.randomUUID().toString().getBytes()).toString());
        skullOwner.put("Name", "clay_skull_fix");
        NbtCompound properties = NbtFactory.ofCompound("");
        NbtCompound image = NbtFactory.ofCompound("");
        image.put("Value", decodedImage);
        NbtList<?> textures = NbtFactory.ofList("", image);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        compound.put("SkullOwner", skullOwner);
        return compound;
    }
}
