package net.cubo.woolbrawl.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class Kit {
    public static final int SLOT_HELMET = -1;
    public static final int SLOT_CHEST = -2;
    public static final int SLOT_LEGS = -3;
    public static final int SLOT_BOOTS = -4;
    public static final int SLOT_OFFHAND = -5;
    public static final int SLOT_MAINHAND = -6;
    public static final int SLOT_INVENTORY = -7;

    private final int id;
    private final String name;
    private final Material icon;
    private final String[] lore;
    private final List<Entry> entries = new ArrayList<>();

    public Kit(int id, String name, Material icon, String[] lore) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.lore = lore;
    }

    public int id() { return id; }
    public String displayName() { return name; }
    public Material icon() { return icon; }
    public String[] lore() { return lore; }

    public Kit helmet(ItemStack it)   { entries.add(new Entry(SLOT_HELMET, it));   return this; }
    public Kit chest(ItemStack it)    { entries.add(new Entry(SLOT_CHEST, it));    return this; }
    public Kit legs(ItemStack it)     { entries.add(new Entry(SLOT_LEGS, it));     return this; }
    public Kit boots(ItemStack it)    { entries.add(new Entry(SLOT_BOOTS, it));    return this; }
    public Kit offhand(ItemStack it)  { entries.add(new Entry(SLOT_OFFHAND, it));  return this; }
    public Kit mainhand(ItemStack it) { entries.add(new Entry(SLOT_MAINHAND, it)); return this; }
    public Kit item(ItemStack it)     { entries.add(new Entry(SLOT_INVENTORY, it)); return this; }

    public void equip(PlayerInventory inv) {
        for (Entry e : entries) {
            switch (e.slot) {
                case SLOT_HELMET   -> inv.setHelmet(e.item.clone());
                case SLOT_CHEST    -> inv.setChestplate(e.item.clone());
                case SLOT_LEGS     -> inv.setLeggings(e.item.clone());
                case SLOT_BOOTS    -> inv.setBoots(e.item.clone());
                case SLOT_OFFHAND  -> inv.setItemInOffHand(e.item.clone());

                case SLOT_MAINHAND -> inv.setItem(0, e.item.clone());
                default            -> inv.addItem(e.item.clone());
            }
        }
    }

    public List<ItemStack> preview() {
        List<ItemStack> result = new ArrayList<>();
        addByType(result, SLOT_HELMET);
        addByType(result, SLOT_CHEST);
        addByType(result, SLOT_LEGS);
        addByType(result, SLOT_BOOTS);
        addByType(result, SLOT_MAINHAND);
        addByType(result, SLOT_OFFHAND);
        addByType(result, SLOT_INVENTORY);
        return result;
    }

    private void addByType(List<ItemStack> out, int type) {
        for (Entry e : entries) {
            if (e.slot == type) out.add(e.item.clone());
        }
    }

    public static ItemStack item(Material m) { return new ItemStack(m); }

    private static class Entry {
        final int slot;
        final ItemStack item;
        Entry(int slot, ItemStack item) { this.slot = slot; this.item = item; }
    }
}
