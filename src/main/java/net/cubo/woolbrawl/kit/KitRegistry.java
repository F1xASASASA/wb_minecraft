package net.cubo.woolbrawl.kit;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class KitRegistry {

    private final Map<Integer, Kit> kits = new LinkedHashMap<>();

    public KitRegistry() { register(); }

    public Kit get(int id) { return kits.get(id); }
    public Collection<Kit> all() { return kits.values(); }
    public int count() { return kits.size(); }

    public List<Kit> roll(int count) {
        List<Kit> pool = new ArrayList<>(kits.values());
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()));
    }

    private void register() {
        Kit k1 = new Kit(1, "§bВихрь", Material.DIAMOND_SWORD, new String[]{"§7Алм.меч + жел.кираса", "§7Скорость II"});
        k1.mainhand(new ItemStack(Material.DIAMOND_SWORD));
        k1.chest(new ItemStack(Material.IRON_CHESTPLATE));
        k1.item(potion(Material.POTION, PotionEffectType.SPEED, 3600, 0));
        k1.boots(new ItemStack(Material.LEATHER_BOOTS));
        kits.put(1, k1);

        Kit k2 = new Kit(2, "§5Слепота", Material.SPLASH_POTION, new String[]{"§7Алм.меч + кольчуга", "§73x взр.слепоты"});
        k2.mainhand(new ItemStack(Material.DIAMOND_SWORD));
        k2.chest(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        k2.helmet(new ItemStack(Material.CHAINMAIL_HELMET));
        k2.boots(new ItemStack(Material.LEATHER_BOOTS));
        ItemStack blindness = potion(Material.SPLASH_POTION, PotionEffectType.BLINDNESS, 200, 0);
        blindness.setAmount(3);
        k2.item(blindness);
        kits.put(2, k2);

        Kit k3 = new Kit(3, "§8Некромант", Material.WITHER_SKELETON_SKULL, new String[]{"§7Череп визера + камен.меч", "§72x лингер. урон II"});
        k3.mainhand(new ItemStack(Material.STONE_SWORD));
        k3.helmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
        k3.chest(new ItemStack(Material.LEATHER_CHESTPLATE));
        k3.legs(new ItemStack(Material.LEATHER_LEGGINGS));
        k3.boots(new ItemStack(Material.LEATHER_BOOTS));
        ItemStack harm = potion(Material.LINGERING_POTION, PotionEffectType.INSTANT_DAMAGE, 1, 1);
        harm.setAmount(2);
        k3.item(harm);
        kits.put(3, k3);

        Kit k4 = new Kit(4, "§eВоевода", Material.SHIELD, new String[]{"§7Топор + щит (10 прочн.)"});
        k4.mainhand(new ItemStack(Material.STONE_AXE));
        ItemStack shield = new ItemStack(Material.SHIELD);
        if (shield.getItemMeta() instanceof Damageable dm) {
            dm.setMaxDamage(10);
            shield.setItemMeta(dm);
        }
        k4.offhand(shield);
        k4.chest(new ItemStack(Material.IRON_CHESTPLATE));
        k4.boots(new ItemStack(Material.LEATHER_BOOTS));
        kits.put(4, k4);

        Kit k5 = new Kit(5, "§dЛекарь", Material.SPLASH_POTION, new String[]{"§7Реген II + лечение"});
        k5.mainhand(new ItemStack(Material.STONE_SWORD));
        k5.chest(new ItemStack(Material.IRON_CHESTPLATE));
        k5.boots(new ItemStack(Material.LEATHER_BOOTS));
        k5.item(potion(Material.SPLASH_POTION, PotionEffectType.REGENERATION, 450, 1));
        k5.item(potion(Material.SPLASH_POTION, PotionEffectType.INSTANT_HEALTH, 1, 1));
        kits.put(5, k5);

        Kit k6 = new Kit(6, "§6Купидон", Material.TOTEM_OF_UNDYING, new String[]{"§7Арбалет + стрелы хила", "§7Тотем"});
        k6.mainhand(new ItemStack(Material.IRON_SWORD));
        k6.helmet(new ItemStack(Material.GOLDEN_HELMET));
        k6.boots(new ItemStack(Material.LEATHER_BOOTS));
        k6.item(new ItemStack(Material.CROSSBOW));
        ItemStack healArrow = potion(Material.TIPPED_ARROW, PotionEffectType.INSTANT_HEALTH, 1, 1);
        healArrow.setAmount(5);
        k6.item(healArrow);
        k6.offhand(new ItemStack(Material.TOTEM_OF_UNDYING));
        kits.put(6, k6);

        Kit k7 = new Kit(7, "§bПосейдон", Material.TRIDENT, new String[]{"§7Трезубец (верность II)"});
        ItemStack trident = new ItemStack(Material.TRIDENT);
        trident.addUnsafeEnchantment(Enchantment.LOYALTY, 2);
        k7.mainhand(trident);
        k7.chest(new ItemStack(Material.IRON_CHESTPLATE));
        k7.boots(new ItemStack(Material.LEATHER_BOOTS));
        kits.put(7, k7);

        Kit k8 = new Kit(8, "§6Кабан", Material.GOLDEN_AXE, new String[]{"§7Зол.топор (отдача I)", "§7Незер.нагрудник"});
        ItemStack axe = new ItemStack(Material.GOLDEN_AXE);
        axe.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        k8.mainhand(axe);
        k8.chest(new ItemStack(Material.NETHERITE_CHESTPLATE));
        k8.helmet(new ItemStack(Material.LEATHER_HELMET));
        k8.boots(new ItemStack(Material.LEATHER_BOOTS));
        kits.put(8, k8);

        Kit k9 = new Kit(9, "§cШахтёр", Material.TNT, new String[]{"§7Кирка + 3x ТНТ", "§7Зол.яблоко"});
        k9.mainhand(new ItemStack(Material.NETHERITE_PICKAXE));
        ItemStack blastHelmet = new ItemStack(Material.NETHERITE_HELMET);
        blastHelmet.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 10);
        k9.helmet(blastHelmet);
        k9.chest(new ItemStack(Material.IRON_CHESTPLATE));
        k9.boots(new ItemStack(Material.LEATHER_BOOTS));
        k9.item(new ItemStack(Material.GOLDEN_APPLE));
        k9.item(new ItemStack(Material.TNT, 3));
        kits.put(9, k9);

        Kit k10 = new Kit(10, "§9Балиста", Material.CROSSBOW, new String[]{"§7Арбалет (пронзание)", "§77x стрел замедления"});
        k10.mainhand(new ItemStack(Material.GOLDEN_AXE));
        k10.boots(new ItemStack(Material.LEATHER_BOOTS));
        ItemStack cb = new ItemStack(Material.CROSSBOW);
        cb.addUnsafeEnchantment(Enchantment.PIERCING, 1);
        k10.item(cb);
        ItemStack slowArrow = potion(Material.TIPPED_ARROW, PotionEffectType.SLOWNESS, 800, 0);
        slowArrow.setAmount(7);
        k10.item(slowArrow);
        kits.put(10, k10);

        Kit k11 = new Kit(11, "§aЧерепаха", Material.TURTLE_HELMET, new String[]{"§7Череп.мощь: сопрот. IV 20с"});
        k11.mainhand(new ItemStack(Material.WOODEN_SWORD));
        k11.chest(new ItemStack(Material.GOLDEN_CHESTPLATE));
        k11.helmet(new ItemStack(Material.TURTLE_HELMET));
        k11.boots(new ItemStack(Material.LEATHER_BOOTS));
        ItemStack turtleP = new ItemStack(Material.POTION);
        PotionMeta tpm = (PotionMeta) turtleP.getItemMeta();
        tpm.addCustomEffect(new PotionEffect(PotionEffectType.RESISTANCE, 400, 3), true);
        tpm.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 3), true);
        turtleP.setItemMeta(tpm);
        k11.item(turtleP);
        kits.put(11, k11);

        Kit k12 = new Kit(12, "§2Паук-отравитель", Material.COBWEB, new String[]{"§7Лук + стрелы отравления", "§72x паутины"});
        k12.mainhand(new ItemStack(Material.STONE_AXE));
        k12.chest(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        k12.helmet(new ItemStack(Material.NETHERITE_HELMET));
        k12.boots(new ItemStack(Material.LEATHER_BOOTS));
        k12.item(new ItemStack(Material.BOW));
        ItemStack poisonArrow = potion(Material.TIPPED_ARROW, PotionEffectType.POISON, 800, 1);
        poisonArrow.setAmount(2);
        k12.item(poisonArrow);
        k12.item(new ItemStack(Material.COBWEB, 2));
        kits.put(12, k12);

        Kit k13 = new Kit(13, "§fЛучник", Material.BOW, new String[]{"§7Лук + 12 стрел"});
        k13.mainhand(new ItemStack(Material.STONE_SWORD));
        k13.chest(new ItemStack(Material.LEATHER_CHESTPLATE));
        k13.boots(new ItemStack(Material.LEATHER_BOOTS));
        k13.item(new ItemStack(Material.BOW));
        k13.item(new ItemStack(Material.ARROW, 12));
        kits.put(13, k13);

        Kit k14 = new Kit(14, "§5Алхимик", Material.SPLASH_POTION, new String[]{"§7Реген (питьё) + 2x взр.урон II"});
        k14.mainhand(new ItemStack(Material.STONE_SWORD));
        k14.chest(new ItemStack(Material.LEATHER_CHESTPLATE));
        k14.boots(new ItemStack(Material.LEATHER_BOOTS));
        k14.item(potion(Material.POTION, PotionEffectType.REGENERATION, 450, 1));
        ItemStack harm14 = potion(Material.SPLASH_POTION, PotionEffectType.INSTANT_DAMAGE, 1, 1);
        ItemStack harm15 = potion(Material.SPLASH_POTION, PotionEffectType.INSTANT_DAMAGE, 1, 0);
        harm14.setAmount(1);
        harm15.setAmount(1);
        k14.item(harm14);
        kits.put(14, k14);
    }

    private ItemStack potion(Material m, PotionEffectType type, int durationTicks, int amplifier) {
        ItemStack p = new ItemStack(m);
        PotionMeta pm = (PotionMeta) p.getItemMeta();
        pm.addCustomEffect(new PotionEffect(type, durationTicks, amplifier), true);
        p.setItemMeta(pm);
        return p;
    }

    public static void dyeLeather(org.bukkit.inventory.PlayerInventory inv, Color color) {
        dye(inv.getHelmet(), color);
        dye(inv.getChestplate(), color);
        dye(inv.getLeggings(), color);
        dye(inv.getBoots(), color);
    }

    private static void dye(ItemStack it, Color color) {
        if (it == null) return;
        if (it.getItemMeta() instanceof LeatherArmorMeta lam) {
            lam.setColor(color);
            it.setItemMeta(lam);
        }
    }
}
