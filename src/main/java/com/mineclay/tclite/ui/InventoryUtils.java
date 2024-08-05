package com.mineclay.tclite.ui;

import com.cryptomorin.xseries.XMaterial;
import com.mineclay.tclite.mcnative.McNative;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryUtils {
    public static String replaceAll(String original, Map<String, String> variables) {
        String result = original;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static ItemStack replaceAllInItemStack(ItemStack item, Map<String, String> variables) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String title = meta.getDisplayName();
        if (title != null) {
            title = replaceAll(title, variables);
        }
        meta.setDisplayName(title);

        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            lore.replaceAll(original -> replaceAll(original, variables));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }


    public static int count(Inventory inv) {
        if (inv == null) return 0;
        int count = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null) count++;
        }
        return count;
    }

    public static List<ItemStack> getItems(Inventory inv, int endSlot) {
        if (inv.getSize() < endSlot) endSlot = inv.getSize();

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < endSlot; i++) {
            if (inv.getItem(i) != null) items.add(inv.getItem(i));
        }
        return items;
    }

    public static void sendSlotChange(ItemStack item, int slot, Player p) {
        sendSlotChange(McNative.getActiveWindowId(p), slot, item, p);
    }

    public static void sendSlotChange(int windowId, int slot, ItemStack item, Player p) {
        McNative.sendSlotChange(windowId, slot, item, p);
    }

    public static void sendCursorChange(ItemStack item, Player p) {
        sendSlotChange(-1, -1, item, p);
    }

    public static void clearAllFromPlayer(Player player) {
        player.getInventory().clear();
        EntityEquipment equip = player.getEquipment();
        equip.clear();

        player.setLevel(0);
        player.setExp(0);

        for (PotionEffectType x : PotionEffectType.values()) {
            if (x == null) {
                continue;
            }
            if (player.hasPotionEffect(x)) {
                player.removePotionEffect(x);
            }
        }
    }

    public static boolean checkInventory(Player player) {
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i) != null) {
                return false;
            }
        }

        EntityEquipment equip = player.getEquipment();
        return equip.getHelmet() == null && equip.getChestplate() == null && equip.getLeggings() == null && equip.getBoots() == null;

    }

    public static String getItemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getDisplayName() == null || meta.getDisplayName().isEmpty()) {
            return getMaterialName(item.getType());
        }
        return meta.getDisplayName();

    }

    public static String getMaterialName(Material m) {
        String def = m.toString().replace("_", " ").toLowerCase();
        def = def.substring(0, 1).toUpperCase() + def.substring(1);
        Pattern p = Pattern.compile("[ ]");
        Matcher matcher = p.matcher(def);
        while (matcher.find()) {
            def = def.substring(0, matcher.start() + 1) + def.substring(matcher.start() + 1, matcher.start() + 2).toUpperCase() + def.substring(matcher.start() + 2);
        }
        return def;
    }

    public static int calcLeft(Inventory inv, ItemStack i) {
        if (i == null) return 0;

        i.setAmount(1);
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;
            if (item.isSimilar(i)) count += item.getAmount();
        }

        return count;
    }

    public static boolean canPutIn(PlayerInventory inv, ItemStack item, int amount) {
        item = new ItemStack(item);
        item.setAmount(1);

        int avail = 0;
        for (int i = 0; i < inv.getStorageContents().length; i++) {
            ItemStack cur = inv.getStorageContents()[i];
            if (cur == null || cur.getType().equals(Material.AIR)) {
                avail += item.getMaxStackSize();
                continue;
            }

            if (cur.isSimilar(item)) {
                avail += item.getMaxStackSize() - cur.getAmount();
            }
        }

        return avail >= amount;
    }

    public static boolean canPutIn(Inventory inv, ItemStack item, int amount) {
        item = new ItemStack(item);
        item.setAmount(1);

        int avail = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack cur = inv.getItem(i);
            if (cur == null || cur.getType().equals(Material.AIR)) {
                avail += item.getMaxStackSize();
                continue;
            }

            if (cur.isSimilar(item)) {
                avail += item.getMaxStackSize() - cur.getAmount();
            }
        }

        return avail >= amount;
    }

    public static boolean canPutIn(Inventory inv, List<ItemStack> items) {
        Inventory mirror;
        if (inv instanceof PlayerInventory) {
            mirror = Bukkit.createInventory(null, inv.getStorageContents().length);
            mirror.setContents(inv.getStorageContents());
        } else {
            mirror = Bukkit.createInventory(null, inv.getSize());
            mirror.setContents(inv.getContents());
        }
        for (ItemStack item : items) {
            if (!mirror.addItem(item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static void removeFromPlayer(Player player, ItemStack item, int count) {
        if (count <= 0 || item == null || item.getType() == Material.AIR) return;
        ItemStack[] arr = player.getInventory().getStorageContents();
        count = handleItemStackArr(arr, item, count);
        player.getInventory().setStorageContents(arr);
        if (count <= 0) {
            player.updateInventory();
            return;
        }
        arr = player.getInventory().getExtraContents();
        count = handleItemStackArr(arr, item, count);
        player.getInventory().setExtraContents(arr);
        if (count <= 0) {
            player.updateInventory();
            return;
        }
        arr = player.getInventory().getArmorContents();
        handleItemStackArr(arr, item, count);
        player.getInventory().setArmorContents(arr);
        player.updateInventory();
    }

    private static int handleItemStackArr(ItemStack[] arr, ItemStack item, int count) {
        for (int i = 0; i < arr.length; i++) {
            if (count <= 0) break;
            ItemStack cur = arr[i];
            if (cur == null || cur.getType() == Material.AIR) continue;
            if (cur.isSimilar(item)) {
                int left = cur.getAmount() - count;
                if (left > 0) {
                    cur.setAmount(left);
                    count = 0;
                } else {
                    arr[i] = null;
                    count -= cur.getAmount();
                }
            }
        }
        return count;
    }

    public static int calcFreeSpace(Inventory inv, ItemStack sample) {
        if (sample == null || sample.getType() == Material.AIR) return -1;
        ItemStack[] arr = inv.getStorageContents();
        int amount = 0;
        for (ItemStack cur : arr) {
            if (cur == null || cur.getType() == Material.AIR) {
                amount += sample.getMaxStackSize();
            } else {
                if (cur.isSimilar(sample)) {
                    int free = cur.getMaxStackSize() - cur.getAmount();
                    if (free > 0) amount += free;
                }
            }
        }
        return amount;
    }

    public static ItemStack genBlankButton(Material material, short durability) {
        ItemStack item = new ItemStack(material);
        item.setDurability(durability);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.setLore(Collections.emptyList());
        item.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.values());
        return item;
    }

    public static ItemStack genBlankButton(Material material) {
        return genBlankButton(material, (short) 0);
    }

    public static ItemStack genBlankButton() {
        return genBlankButton(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial());
    }

    public static ItemStack genSimpleItem(Material material, short durability, String title, String... desc) {
        ItemStack item = new ItemStack(material, 1, durability);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        List<String> lore = new ArrayList<>();
        Collections.addAll(lore, desc);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
