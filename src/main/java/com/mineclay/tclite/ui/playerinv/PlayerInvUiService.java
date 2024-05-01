package com.mineclay.tclite.ui.playerinv;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.mineclay.tclite.Loader;
import com.mineclay.tclite.ui.InventoryUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class PlayerInvUiService implements Listener, Loader.Loadable {

    @Data
    private static class RegisteredSlot {
        private final PlayerInvButtonInitializer<?> initializer;
        private final PlayerInvButtonSlot slot;
        private final JavaPlugin plugin;
    }

    private static PlayerInvUiService instance;

    public static PlayerInvUiService inst() {
        return instance;
    }

    {
        instance = this;
    }

    private final JavaPlugin plugin;
    final Map<PlayerInvButtonSlot, RegisteredSlot> registrationMap = new HashMap<>();
    final Map<UUID, Map<PlayerInvButtonSlot, PlayerInvButton>> objectMap = new HashMap<>();

    private final ValidateScanner validateScanner = new ValidateScanner(this);
    private PlayerInventoryOpenListener playerInventoryOpenListener;
    private BukkitTask interactClearTask;

    private final Set<UUID> openingInventoryPlayers = new HashSet<>();

    public void register(PlayerInvButtonInitializer<?> initializer, JavaPlugin plugin) {
        PlayerInvButtonSlot slotMark = initializer.getSlotMark();
        Validate.notNull(initializer, "initializer cannot be null");
        Validate.notNull(plugin, "plugin cannot be null");
        Validate.isTrue(plugin.isEnabled(), "plugin should be enabled");

        if (registrationMap.get(slotMark) != null) throw new RuntimeException("slot conflict");

        registrationMap.put(slotMark, new RegisteredSlot(initializer, slotMark, plugin));

        for (Player entity : Bukkit.getOnlinePlayers()) {
            updateAll(entity);
        }
    }

    public void unregister(PlayerInvButtonInitializer<?> initializer) {
        registrationMap.entrySet().removeIf(en -> en.getValue().initializer == initializer);
        for (Player entity : Bukkit.getOnlinePlayers()) {
            updateAll(entity);
        }
    }

    public <T extends PlayerInvButton> T getButton(Player player, Class<T> clz) {
        Map<PlayerInvButtonSlot, PlayerInvButton> map = objectMap.get(player.getUniqueId());
        if (map == null) return null;
        for (Map.Entry<PlayerInvButtonSlot, PlayerInvButton> en : map.entrySet()) {
            if (en.getValue().getClass().equals(clz)) {
                //noinspection unchecked
                return (T) en.getValue();
            }
        }
        return null;
    }

    private void setOpeningInventory(Player player, boolean openingInventory) {
        if (openingInventory) {
            if (!openingInventoryPlayers.contains(player.getUniqueId())) {
                openingInventoryPlayers.add(player.getUniqueId());
                Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = objectMap.get(player.getUniqueId());
                if (buttonMap == null) return;
                for (Map.Entry<PlayerInvButtonSlot, PlayerInvButton> en : buttonMap.entrySet()) {
                    if (!en.getKey().isHotbar()) continue;
                    en.getValue().update();
                }
            }
        } else {
            if (openingInventoryPlayers.remove(player.getUniqueId())) {
                Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = objectMap.get(player.getUniqueId());
                if (buttonMap == null) return;
                for (Map.Entry<PlayerInvButtonSlot, PlayerInvButton> en : buttonMap.entrySet()) {
                    if (!en.getKey().isHotbar() || !(en.getValue() instanceof HotBarButton)) continue;
                    en.getValue().update();
                }
            }
        }
    }

    private boolean isOpeningInventory(Player player) {
        return openingInventoryPlayers.contains(player.getUniqueId());
    }

    public void update(PlayerInvButton button) {
        boolean openingInventory = isOpeningInventory(button.getPlayer());
        boolean hotbar = button.getSlotMark().isHotbar();
        ItemStack item = openingInventory ? button.getItem() : hotbar && button instanceof HotBarButton ? ((HotBarButton) button).getHotbarItem() : button.getItem();
        item = markAsItemButton(item);

        button.getPlayer().getInventory().setItem(button.getSlotMark().getAbsoluteSlot(), item);
        if (button.getSlotMark().isHotbar()) {
            InventoryUtils.sendSlotChange(0, button.getSlotMark().getSlot() + 36, item, button.getPlayer());
            return;
        }

        int previousSlots = 9;
        if (button.getPlayer().getOpenInventory() != null && button.getPlayer().getOpenInventory().getTopInventory() != null) {
            previousSlots = button.getPlayer().getOpenInventory().getTopInventory().getSize();
        }

        int rawSlot = button.getSlotMark().isHotbar() ? button.getSlotMark().getSlot() + 27 : button.getSlotMark().getSlot();
        rawSlot += previousSlots;

        InventoryUtils.sendSlotChange(item, rawSlot, button.getPlayer());
    }

    public void updateAll(Player player) {
        Map<PlayerInvButtonSlot, PlayerInvButton> objects = new HashMap<>();
        for (RegisteredSlot r : registrationMap.values()) {
            PlayerInvButton button;
            try {
                button = r.initializer.create(player);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "creating inv button from " + r.initializer.getClass().getName(), ex);
                continue;
            }
            button.player = player;
            button.initializer = r.initializer;

            try {
                button.init();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "initializing player inv button " + button.getClass().getName(), ex);
                continue;
            }
            objects.put(r.slot, button);

        }
        objectMap.put(player.getUniqueId(), objects);
        objects.values().forEach(PlayerInvButton::update);

        int held = player.getInventory().getHeldItemSlot();
        PlayerInvButtonSlot slot = new PlayerInvButtonSlot(held, true);
        PlayerInvButton button = objects.get(slot);
        if (button instanceof HotBarButton) {
            ((HotBarButton) button).onFocus();
        }
    }

    public boolean isInvButton(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(item));
        return compound.containsKey("_tclite_button");
    }

    public <T extends PlayerInvButton> T getPlayerButton(Player player, Class<T> clz) {
        Validate.notNull(player, "player cannot be null");
        Validate.notNull(clz, "clz cannot be null");

        Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = objectMap.get(player.getUniqueId());
        if (buttonMap == null) return null;
        for (PlayerInvButton b : buttonMap.values()) {
            if (clz.isInstance(b)) {
                //noinspection unchecked
                return (T) b;
            }
        }
        return null;
    }

    @Override
    public void load() throws Exception {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        playerInventoryOpenListener = new PlayerInventoryOpenListener(plugin);
        ProtocolLibrary.getProtocolManager().addPacketListener(playerInventoryOpenListener);

        validateScanner.start(plugin);
        interactClearTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> handledInThisTick = null, 1, 1);
    }

    @Override
    public void unload() {
        if (interactClearTask != null) {
            interactClearTask.cancel();
            interactClearTask = null;
        }
        validateScanner.stop();
        if (playerInventoryOpenListener != null)
            ProtocolLibrary.getProtocolManager().removePacketListener(playerInventoryOpenListener);

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    void onClosePlayerInventory(InventoryCloseEvent e) {
        setOpeningInventory((Player) e.getPlayer(), false);
    }

    @EventHandler
    void onPluginDisable(PluginDisableEvent e) {
        Iterator<Map.Entry<PlayerInvButtonSlot, RegisteredSlot>> ite = registrationMap.entrySet().iterator();
        while (ite.hasNext()) {
            RegisteredSlot r = ite.next().getValue();
            if (r.plugin.equals(e.getPlugin())) {
                ite.remove();
                clearButton(r);
            }
        }
    }

    private void clearButton(RegisteredSlot registration) {
        for (Map.Entry<UUID, Map<PlayerInvButtonSlot, PlayerInvButton>> en : objectMap.entrySet()) {
            Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = en.getValue();
            buttonMap.remove(registration.slot);
            Player p = Bukkit.getPlayer(en.getKey());
            if (p != null) {
                p.getInventory().setItem(registration.slot.getAbsoluteSlot(), null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onJoin(PlayerJoinEvent e) {
        updateAll(e.getPlayer());
    }

    @EventHandler
    void onQuit(PlayerQuitEvent e) {
        openingInventoryPlayers.remove(e.getPlayer().getUniqueId());
        Iterator<Map.Entry<UUID, Map<PlayerInvButtonSlot, PlayerInvButton>>> ite = objectMap.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<UUID, Map<PlayerInvButtonSlot, PlayerInvButton>> entry = ite.next();
            if (entry.getKey().equals(e.getPlayer().getUniqueId())) {
                for (PlayerInvButton button : entry.getValue().values()) {
                    try {
                        button.onPlayerQuit();
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.SEVERE, "exception in player quit hook of player inv button " + button.getClass().getName(), ex);
                    }
                }
                ite.remove();
                continue;
            }
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null || !p.isOnline()) {
                ite.remove();
            }
        }
    }

    @EventHandler
    void onRespawn(PlayerRespawnEvent e) {
        updateAll(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onClick(PlayerItemHeldEvent e) {
        Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = objectMap.get(e.getPlayer().getUniqueId());
        if (buttonMap == null) return;
        PlayerInvButtonSlot previous = new PlayerInvButtonSlot(e.getPreviousSlot(), true);
        PlayerInvButton previousButton = buttonMap.get(previous);
        if (previousButton instanceof HotBarButton) {
            try {
                ((HotBarButton) previousButton).onUnFocus();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "exception in un-focus hook of player inv button " + previousButton.getClass().getName(), ex);
            }
        }

        PlayerInvButtonSlot now = new PlayerInvButtonSlot(e.getNewSlot(), true);
        PlayerInvButton newButton = buttonMap.get(now);
        if (newButton instanceof HotBarButton) {
            try {
                ((HotBarButton) newButton).onFocus();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "exception in focus hook of player inv button " + newButton.getClass().getName(), ex);
            }
        }
    }

    @EventHandler
    void onInvClick(InventoryClickEvent e) {
        if (e.getAction() == InventoryAction.HOTBAR_SWAP) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!(e.getClickedInventory() instanceof PlayerInventory)) return;
        PlayerInventory inventory = e.getWhoClicked().getInventory();
        ItemStack clicked = inventory.getItem(e.getSlot());
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (!isInvButton(clicked)) return;
        e.setCancelled(true);

        if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
            InventoryUtils.sendCursorChange(e.getView().getCursor(), (Player) e.getWhoClicked());
            return;
        }

        if (e.getSlot() < 0 || e.getSlot() > 36) return;
        boolean hotbar = e.getSlot() < 9;
        int slot = hotbar ? e.getSlot() : e.getSlot() - 9;
        PlayerInvButtonSlot invButtonSlot = new PlayerInvButtonSlot(slot, hotbar);

        Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = objectMap.get(e.getWhoClicked().getUniqueId());
        PlayerInvButton button = buttonMap.get(invButtonSlot);
        if (button == null) {
            e.getWhoClicked().getInventory().setItem(e.getSlot(), null);
            return;
        }

        PlayerInvResponse response = button.onInvClick(e.getAction());
        if (response == PlayerInvResponse.CLOSE && e.getWhoClicked().getOpenInventory() == null)
            e.getView().close();
    }

    @EventHandler
    void filterHotbarSwap(InventoryClickEvent e) {
        if (e.getAction() != InventoryAction.HOTBAR_SWAP) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!(e.getClickedInventory() instanceof PlayerInventory)) return;
        PlayerInventory inventory = e.getWhoClicked().getInventory();
        ItemStack clicked = inventory.getItem(e.getSlot());
        if (clicked != null && clicked.getType() != Material.AIR) {
            if (isInvButton(clicked)) {
                e.setCancelled(true);
                return;
            }
        }
        ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
        if (hotbarItem != null && hotbarItem.getType() != Material.AIR) {
            if (isInvButton(hotbarItem)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!(e.getInventory() instanceof PlayerInventory)) return;

        for (int slot : e.getInventorySlots()) {
            ItemStack originalItem = e.getInventory().getItem(slot);
            if (originalItem != null && originalItem.getType() != Material.AIR) {
                if (isInvButton(originalItem)) {
                    e.setCancelled(true);
                    ((Player) e.getWhoClicked()).updateInventory();
                    return;
                }
            }
        }
    }

    private Player handledInThisTick;

    @EventHandler(priority = EventPriority.LOWEST)
    void onInteract(PlayerInteractEvent e) {
        if (!e.getPlayer().isOnline()) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (!isInvButton(item)) return;
        e.setCancelled(true);

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (handledInThisTick != null && handledInThisTick == e.getPlayer()) {
                handledInThisTick = null;
                return;
            }
            handledInThisTick = e.getPlayer();
        }

        if (e.getAction() == null || e.getAction() == Action.PHYSICAL) return;

        boolean right = !(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK);

        Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = objectMap.get(e.getPlayer().getUniqueId());
        PlayerInvButtonSlot slot = new PlayerInvButtonSlot(e.getPlayer().getInventory().getHeldItemSlot(), true);
        PlayerInvButton button = buttonMap.get(slot);
        if (button == null) {
            e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().getHeldItemSlot(), null);
            return;
        }
        button.onHotbarClick(right);
    }

    @EventHandler
    void onSwap(PlayerSwapHandItemsEvent e) {
        if (e.getOffHandItem() != null && e.getOffHandItem().getType() != Material.AIR) {
            if (isInvButton(e.getOffHandItem())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onDrop(PlayerDropItemEvent e) {
        ItemStack dropped = e.getItemDrop().getItemStack();
        if (dropped == null || dropped.getType() == Material.AIR) return;

        if (isInvButton(dropped)) {
            e.setCancelled(true);

            Map<PlayerInvButtonSlot, PlayerInvButton> buttonMap = objectMap.get(e.getPlayer().getUniqueId());
            if (buttonMap == null) return;

            PlayerInvButtonSlot slot = new PlayerInvButtonSlot(e.getPlayer().getInventory().getHeldItemSlot(), true);
            PlayerInvButton buttonNow = buttonMap.get(slot);
            if (buttonNow instanceof HotBarButton) {
                try {
                    ((HotBarButton) buttonNow).onDrop();
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.SEVERE, "exception in drop hook of player inv button " + buttonNow.getClass().getName(), ex);
                }
            }
        }
    }

    @EventHandler
    void onInteractItemFrame(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() == null) return;
        if (e.getRightClicked().getType() != EntityType.ITEM_FRAME) return;
        ItemFrame itemFrame = (ItemFrame) e.getRightClicked();
        if (itemFrame.getItem() != null && itemFrame.getItem().getType() != Material.AIR) return;
        ItemStack toPut = e.getPlayer().getEquipment().getItemInMainHand();
        if (isInvButton(toPut)) e.setCancelled(true);
    }

    @EventHandler
    void onPickupItem(PlayerPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (item == null || item.getType() == Material.AIR) return;
        if (isInvButton(item)) {
            e.setCancelled(true);
            e.getItem().remove();
        }
    }

    @EventHandler
    void onCommand(PlayerCommandPreprocessEvent e) {
        ItemStack held = e.getPlayer().getEquipment().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) return;
        if (!isInvButton(held)) return;

        if (e.getMessage().toLowerCase().startsWith("/hat") ||
                e.getMessage().toLowerCase().startsWith("/ehat") ||
                e.getMessage().toLowerCase().startsWith("/essentials:hat") ||
                e.getMessage().toLowerCase().startsWith("/essentials:ehat")
        ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    void onStandardInvOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        setOpeningInventory((Player) e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onDeathDrops(PlayerDeathEvent e) {
        e.getDrops().removeIf(this::isInvButton);
    }

    RegisteredSlot getRegistration(PlayerInvButtonSlot slot) {
        return registrationMap.get(slot);
    }

    private static ItemStack markAsItemButton(ItemStack item) {
        Logger logger = Logger.getLogger(PlayerInvUiService.class.getName());

        if (item == null || item.getType() == Material.AIR) return null;
        Material preType = item.getType();
        try {
            item = MinecraftReflection.getBukkitItemStack(item);
            if (item.getType() == Material.AIR) {
                logger.log(Level.WARNING,
                        "ItemStack became AIR after converting to CraftItemStack, type=" + preType);
                return null;
            }
            NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
            compound.put("tcbaselib_button", 1);
            NbtFactory.setItemTag(item, compound);
            return item;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "processing ItemStack mark, item=" + item, e);
            return null;
        }
    }

    private class PlayerInventoryOpenListener extends PacketAdapter {
        PlayerInventoryOpenListener(JavaPlugin plugin) {
            super(plugin, PacketType.Play.Client.CLIENT_COMMAND);
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {
            EnumWrappers.ClientCommand clientCommand = event.getPacket().getEnumModifier(EnumWrappers.ClientCommand.class, 0).read(0);
            if (clientCommand == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                if (event.isAsync()) {
                    Bukkit.getScheduler().runTask(plugin, () -> handle(event.getPlayer()));
                } else {
                    handle(event.getPlayer());
                }
            }
        }

        private void handle(Player player) {
            setOpeningInventory(player, true);
        }
    }
}
