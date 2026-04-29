package net.cubo.woolbrawl.kit;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.game.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitSelectGUI {

    public static final NamespacedKey KIT_ID_KEY = new NamespacedKey("woolbrawl", "kit_id");
    public static final NamespacedKey LOCKED_KEY = new NamespacedKey("woolbrawl", "locked");
    public static final Component TITLE = Component.text("§c§lВыбор кита");

    public static final int ROWS = 6;
    public static final int COLS = 9;

    public static void open(Player p, List<Kit> pool, int currentKitId, Team team) {
        Inventory inv = Bukkit.createInventory(null, ROWS * COLS, TITLE);

        ItemStack filler = filler();
        for (int i = 0; i < ROWS * COLS; i++) inv.setItem(i, filler);

        int kitCount = Math.min(pool.size(), ROWS);
        for (int row = 0; row < kitCount; row++) {
            Kit kit = pool.get(row);
            boolean selected = kit.id() == currentKitId;

            String takenBy = null;
            if (team != null && WoolBrawlPlugin.get().game() != null) {
                String n = WoolBrawlPlugin.get().game().kitTakenByName(kit.id(), team);
                if (n != null && !n.equals(p.getName())) takenBy = n;
            }
            boolean locked = takenBy != null;

            inv.setItem(row * COLS, headerFor(kit, selected, locked, takenBy));
            inv.setItem(row * COLS + 1, separator(selected, locked));

            List<ItemStack> preview = kit.preview();
            for (int i = 0; i < preview.size() && i < (COLS - 2); i++) {
                ItemStack prev = preview.get(i).clone();
                tagKitId(prev, kit.id(), locked);
                inv.setItem(row * COLS + 2 + i, prev);
            }
        }

        p.openInventory(inv);
    }

    private static ItemStack headerFor(Kit kit, boolean selected, boolean locked, String lockedByName) {
        Material icon;
        if (locked) icon = Material.BARRIER;
        else if (selected) icon = Material.LIME_STAINED_GLASS_PANE;
        else icon = kit.icon();

        ItemStack it = new ItemStack(icon);
        ItemMeta meta = it.getItemMeta();

        String namePrefix = locked ? "§7§m" : "";
        meta.displayName(Component.text(namePrefix + kit.displayName()).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        for (String line : kit.lore()) {
            lore.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        if (locked) {
            lore.add(Component.text("§c§lЗАНЯТО §7(" + lockedByName + ")").decoration(TextDecoration.ITALIC, false));
        } else if (selected) {
            lore.add(Component.text("§a§l✓ ВЫБРАН").decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("§eКлик — выбрать").decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        meta.getPersistentDataContainer().set(KIT_ID_KEY, PersistentDataType.INTEGER, kit.id());
        if (locked) meta.getPersistentDataContainer().set(LOCKED_KEY, PersistentDataType.BYTE, (byte) 1);
        it.setItemMeta(meta);
        return it;
    }

    private static ItemStack separator(boolean selected, boolean locked) {
        Material m;
        if (locked) m = Material.RED_STAINED_GLASS_PANE;
        else if (selected) m = Material.LIME_STAINED_GLASS_PANE;
        else m = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Component.text(" "));
        it.setItemMeta(meta);
        return it;
    }

    private static void tagKitId(ItemStack it, int kitId, boolean locked) {
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(KIT_ID_KEY, PersistentDataType.INTEGER, kitId);
        if (locked) meta.getPersistentDataContainer().set(LOCKED_KEY, PersistentDataType.BYTE, (byte) 1);
        it.setItemMeta(meta);
    }

    private static ItemStack filler() {
        ItemStack it = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Component.text(" "));
        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack opener() {
        ItemStack it = new ItemStack(Material.EMERALD);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Component.text("§a§lВыбор кита").decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("§7ПКМ — открыть меню").decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey("woolbrawl", "opener"),
                PersistentDataType.BYTE, (byte) 1);
        it.setItemMeta(meta);
        return it;
    }

    public static boolean isOpener(ItemStack it) {
        if (it == null || it.getType() != Material.EMERALD) return false;
        if (!it.hasItemMeta()) return false;
        return it.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey("woolbrawl", "opener"), PersistentDataType.BYTE);
    }
}
