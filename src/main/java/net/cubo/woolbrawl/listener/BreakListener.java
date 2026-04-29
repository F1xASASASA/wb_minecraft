package net.cubo.woolbrawl.listener;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.game.GameState;
import net.cubo.woolbrawl.game.PlayerData;
import net.cubo.woolbrawl.game.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class BreakListener implements Listener {

    private final WoolBrawlPlugin plugin;

    public BreakListener(WoolBrawlPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        GameState state = plugin.game().state();
        if (state == GameState.IDLE || state == GameState.ENDING) {
            e.setCancelled(true);
            return;
        }

        Block b = e.getBlock();
        boolean inZone = plugin.game().zone().isInZone(b);

        if (!inZone) {
            e.setCancelled(true);
            return;
        }

        if (b.getType() == Material.BLACK_WOOL) {
            e.setCancelled(true);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
            return;
        }

        if (isWool(b.getType())) {
            e.setDropItems(false);
            e.setExpToDrop(0);

            if (state == GameState.PLAYING) {
                PlayerData d = plugin.game().data(e.getPlayer());
                if (d == null || !d.alive()) {
                    e.setCancelled(true);
                    return;
                }

                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOL_BREAK, 0.5f, 1f);
            }
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (plugin.game().state() == GameState.IDLE) return;

        Block b = e.getBlock();
        Material m = b.getType();

        if (m == Material.TNT) return;

        if (m == Material.COBWEB) {
            final org.bukkit.block.Block block = b;
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (block.getType() == Material.COBWEB) {
                    block.setType(Material.AIR, false);
                    block.getWorld().spawnParticle(org.bukkit.Particle.CLOUD,
                            block.getLocation().add(0.5, 0.5, 0.5), 6, 0.2, 0.2, 0.2, 0.01);
                }
            }, 15 * 20L);
            return;
        }

        if (plugin.game().zone().isInZone(b)) {
            PlayerData d = plugin.game().data(e.getPlayer());
            if (d == null) {
                e.setCancelled(true);
                return;
            }
            Team team = d.team();
            if (m != team.wool()) {
                e.setCancelled(true);
                e.getPlayer().sendActionBar(Component.text("Только шерсть своего цвета!").color(NamedTextColor.RED));
                return;
            }

            return;
        }

        e.setCancelled(true);
        e.getPlayer().sendActionBar(Component.text("Ставить блоки можно только в зоне захвата").color(NamedTextColor.RED));
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.game().state() == GameState.LOBBY || plugin.game().state() == GameState.ENDING) {
            e.setCancelled(true);
        }
    }

    private static boolean isWool(Material m) {
        return m == Material.WHITE_WOOL || m == Material.RED_WOOL || m == Material.BLUE_WOOL
                || m == Material.BLACK_WOOL || m == Material.ORANGE_WOOL || m == Material.MAGENTA_WOOL
                || m == Material.LIGHT_BLUE_WOOL || m == Material.YELLOW_WOOL || m == Material.LIME_WOOL
                || m == Material.PINK_WOOL || m == Material.GRAY_WOOL || m == Material.LIGHT_GRAY_WOOL
                || m == Material.CYAN_WOOL || m == Material.PURPLE_WOOL || m == Material.BROWN_WOOL
                || m == Material.GREEN_WOOL;
    }
}
