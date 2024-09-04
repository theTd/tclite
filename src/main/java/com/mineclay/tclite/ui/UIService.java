package com.mineclay.tclite.ui;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.mineclay.tclite.Loader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

public class UIService implements Loader.Loadable, Listener {

    private static UIService serviceInstance;

    public static UIService inst() {
        if (serviceInstance == null) throw new IllegalStateException("UIService is not initialized");
        return serviceInstance;
    }

    private final JavaPlugin plugin;

    public UIService(JavaPlugin plugin) {
        serviceInstance = this;
        this.plugin = plugin;
    }

    private final List<AbstractGUI> activeUis = new ArrayList<>();
    private final ValidateTask validateTask = new ValidateTask();
    private final Map<UUID, AbstractGUI> reopenMap = new HashMap<>();

    private PacketListener packetListener;

    @Override
    public void load() throws Exception {
        ProtocolLibrary.getProtocolManager().addPacketListener((packetListener = new PacketAdapter(plugin, PacketType.Play.Server.OPEN_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent e) {
                if (MinecraftVersion.getCurrentVersion().getMinor() == 18) {
                    int id = e.getPacket().getIntegers().read(0);
                    int type = e.getPacket().getIntegers().read(1);
                    if (type < 7) {
                        InventoryView view = e.getPlayer().getOpenInventory();
                        if (view == null) return;
                        Inventory topInventory = view.getTopInventory();
                        if (topInventory == null) return;
                        InventoryHolder holder = topInventory.getHolder();
                        if (holder == null) return;
                        if (!(holder instanceof AbstractGUI)) return;
                        ((AbstractGUI) holder).windowId = id;
                    }
                } else {
                    String type = e.getPacket().getStrings().read(0);
                    int id = e.getPacket().getIntegers().read(0);
                    if (type.equals("minecraft:container")) {
                        InventoryView view = e.getPlayer().getOpenInventory();
                        if (view == null) return;
                        Inventory topInventory = view.getTopInventory();
                        if (topInventory == null) return;
                        InventoryHolder holder = topInventory.getHolder();
                        if (holder == null) return;
                        if (!(holder instanceof AbstractGUI)) return;
                        ((AbstractGUI) holder).windowId = id;
                    }
                }
            }
        }));
        Bukkit.getPluginManager().registerEvents(this, plugin);
        validateTask.runTaskTimer(plugin, 5, 5);
    }

    @Override
    public void unload() {
        for (AbstractGUI gui : getActive(AbstractGUI.class)) {
            gui.close();
        }

        validateTask.cancel();
        HandlerList.unregisterAll(this);
        ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
    }

    public void open(AbstractGUI gui) {
        Player p = gui.getPlayer();
        if (p != null && p.isOnline()) {
            if (!gui.inited) gui.init();
            markReopen(p, gui);
            p.openInventory(gui.getInventory());
            gui.opening = true;
            activeUis.add(gui);
        }
    }

    public void close(AbstractGUI gui) {
        Player p = gui.getPlayer();
        if (p == null || !p.isOnline()) return;
        InventoryView view = p.getOpenInventory();
        if (view == null) return;
        Inventory top = view.getTopInventory();
        if (top == null) return;
        InventoryHolder holder = top.getHolder();
        if (!(holder instanceof AbstractGUI)) return;
        if (holder.equals(gui)) p.closeInventory();
    }

    public void close(Class<? extends AbstractGUI> clz) {
        getActive(clz).forEach(AbstractGUI::close);
    }

    public <T extends AbstractGUI> List<T> getActive(Class<T> clz) {
        List<T> l = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            InventoryView v = p.getOpenInventory();
            if (v == null) continue;
            Inventory top = v.getTopInventory();
            if (top == null) continue;
            InventoryHolder holder = top.getHolder();
            if (!clz.isInstance(holder)) continue;
            //noinspection unchecked
            l.add((T) holder);
        }
        return l;
    }

    public AbstractGUI getActive(Player player) {
        InventoryView v = player.getOpenInventory();
        if (v == null) return null;
        Inventory top = v.getTopInventory();
        if (top == null) return null;
        InventoryHolder holder = top.getHolder();
        if (!(holder instanceof AbstractGUI)) return null;
        return (AbstractGUI) holder;
    }

    public void init(AbstractGUI gui) {
        gui.buttons.clear();
        gui.initButtons();
//        Bukkit.getPluginManager().callEvent(new GUIInitButtonsEvent(gui));

        int max = 0;

        for (Integer i : gui.buttons.keySet()) {
            if (i > max) max = i;
        }

        int row = max / 9 + 1;

        gui.inv = Bukkit.createInventory(gui, row * 9, gui.getTitle());

        for (Map.Entry<Integer, AbstractButton> en : gui.buttons.entrySet()) {
            en.getValue().init();
            en.getValue().refresh();
            gui.inv.setItem(en.getKey(), en.getValue().getItem());
        }
        gui.inited = true;
    }

    public void updateButton(AbstractButton button) {
        if (!button.getGUI().isOpening()) return;
        button.refresh();
        Inventory inv = button.getGUI().getInventory();
        button.getGUI().getSlotsByButton(button).forEach(i -> inv.setItem(i, button.getItem()));
    }

    void markReopen(Player player, AbstractGUI gui) {
        reopenMap.put(player.getUniqueId(), gui);
    }

    AbstractGUI getRepoen(Player player) {
        return reopenMap.get(player.getUniqueId());
    }

    private class ValidateTask extends BukkitRunnable {

        @Override
        public void run() {
            Iterator<AbstractGUI> ite = activeUis.listIterator();
            while (ite.hasNext()) {
                AbstractGUI active = ite.next();
                if (!active.opening) {
                    ite.remove();
                } else {
                    // check valid
                    if (active.getPlayer().isOnline()) {
                        InventoryView view = active.getPlayer().getOpenInventory();
                        if (view != null) {
                            Inventory top = view.getTopInventory();
                            if (top != null && top.getHolder() == active) {
                                continue;
                            }
                        }
                    }

                    active.opening = false;
                    try {
                        active.onClose();
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "exception invoking onClose", e);
                    }
                    ite.remove();
                }
            }
            reopenMap.entrySet().removeIf(en -> Bukkit.getPlayer(en.getKey()) == null);
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    private void onClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Inventory inv = e.getInventory();

        if (!(inv.getHolder() instanceof AbstractGUI)) {
            return;
        }

        AbstractGUI gui = (AbstractGUI) inv.getHolder();
        Player p = (Player) e.getWhoClicked();
        if (!p.equals(gui.getPlayer())) {
            return;
        }

        e.setCancelled(true);

        if (e.getClickedInventory() == null) {
            return;
        }

        if (!e.getClickedInventory().equals(inv)) {

            clearCursor(p);

            if (e.getClickedInventory().equals(e.getView().getBottomInventory())) {
                GUIResponse response;
                try {
                    response = gui.onClickInventory(e.getSlot(), e.getAction());
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.SEVERE, "handling inventory click in gui view " + gui.getClass().getName(), ex);
                    p.closeInventory();
                    return;
                }
                if (response == null) return;

                switch (response.getAction()) {
                    case CLOSE:
                        gui.close();
                        break;
                    case DO_NOTHING:
                        break;
                    case OPEN:
                        final AbstractGUI another = response.getGUI();
                        Bukkit.getScheduler().runTask(plugin, another::open);
                        break;
                    case REFRESH_BUTTON:
                        break;
                    case REFRESH_GUI:
                        gui.init();
                        gui.open();
                        break;
                    default:
                        break;
                }
            }
            return;
        }

        AbstractButton button = gui.getButtonBySlot(e.getSlot());
        if (button == null) return;

        GUIResponse response;
        try {
            response = button.onClick(e.getAction());
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "handling gui click in gui view " + gui.getClass().getName(), ex);
            p.closeInventory();
            return;
        }

        if (response == null) {
            response = GUIResponse.NOTHING;
        }

        switch (response.getAction()) {
            case CLOSE:
                gui.close();
                break;
            case DO_NOTHING:
                clearCursor(p);
                break;
            case OPEN:
                final AbstractGUI another = response.getGUI();
                Bukkit.getScheduler().runTask(plugin, another::open);
                break;
            case REFRESH_BUTTON:
                button.update();
                Bukkit.getScheduler().runTaskLater(plugin, () -> clearCursor(p), 1);
                break;
            case REFRESH_GUI:
                gui.init();
                gui.open();
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onClose(InventoryCloseEvent e) {
        if (e.getInventory() == null || e.getInventory().getHolder() == null) return;
        if (e.getInventory().getHolder() instanceof AbstractGUI) {
            AbstractGUI gui = (AbstractGUI) e.getInventory().getHolder();
            if (!gui.opening) return;
            Player p = (Player) e.getPlayer();
            AbstractGUI reopen = getRepoen(p);
            if (reopen != null && reopen.equals(gui)) return;
            gui.opening = false;
            gui.onClose();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPluginDisabled(PluginDisableEvent e) {
        if (e.getPlugin().equals(plugin)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                InventoryView inventoryView;
                if ((inventoryView = player.getOpenInventory()) == null) continue;
                Inventory inventory;
                if ((inventory = inventoryView.getTopInventory()) == null) continue;
                InventoryHolder holder;
                if ((holder = inventory.getHolder()) == null) continue;
                if (holder instanceof AbstractGUI) player.closeInventory();
            }
        }
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent e) {
        markReopen(e.getPlayer(), null);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AbstractGUI active = getActive(e.getPlayer());
            if (active != null) active.close();
        }, 1);
    }

    private void clearCursor(Player p) {
        InventoryUtils.sendCursorChange(null, p);
    }
}
