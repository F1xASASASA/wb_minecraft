package net.cubo.woolbrawl.listener;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.game.PlayerData;
import net.cubo.woolbrawl.kit.Kit;
import net.cubo.woolbrawl.kit.KitSelectGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;

public class KitSelectListener implements Listener {

    private final WoolBrawlPlugin plugin;

    public KitSelectListener(WoolBrawlPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getView().title() == null) return;

        if (!e.getView().title().equals(KitSelectGUI.TITLE)) return;

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        if (item.getItemMeta().getPersistentDataContainer().has(KitSelectGUI.LOCKED_KEY, PersistentDataType.BYTE)) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            p.sendActionBar(net.kyori.adventure.text.Component.text("Кит уже занят!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }

        Integer kitId = item.getItemMeta().getPersistentDataContainer().get(
                KitSelectGUI.KIT_ID_KEY, PersistentDataType.INTEGER);
        if (kitId == null) return;

        Kit kit = plugin.kits().get(kitId);
        if (kit == null) return;

        PlayerData d = plugin.game().data(p);
        if (d == null) return;

        java.util.UUID taker = plugin.game().kitTakenBy(kit.id(), d.team());
        if (taker != null && !taker.equals(p.getUniqueId())) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            p.sendActionBar(net.kyori.adventure.text.Component.text("Кит уже занят!").color(net.kyori.adventure.text.format.NamedTextColor.RED));

            if (plugin.game().kitPool() != null) {
                KitSelectGUI.open(p, plugin.game().kitPool(), d.kit() != null ? d.kit().id() : -1, d.team());
            }
            return;
        }

        d.setKit(kit);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.8f);
        p.showTitle(Title.title(
                Component.empty(),
                Component.text("✓ Выбран: " + kit.displayName()).color(NamedTextColor.GREEN),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(500))
        ));

        if (plugin.game().kitPool() != null) {
            KitSelectGUI.open(p, plugin.game().kitPool(), kit.id(), d.team());
        }

        for (PlayerData other : plugin.game().allData()) {
            if (other.team() == d.team() && !other.uuid().equals(p.getUniqueId())) {
                Player op = org.bukkit.Bukkit.getPlayer(other.uuid());
                if (op != null && op.getOpenInventory() != null
                        && op.getOpenInventory().title().equals(KitSelectGUI.TITLE)) {
                    KitSelectGUI.open(op, plugin.game().kitPool(),
                            other.kit() != null ? other.kit().id() : -1, other.team());
                }
            }
        }
    }
}
