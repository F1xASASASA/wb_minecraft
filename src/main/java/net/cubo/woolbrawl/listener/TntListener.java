package net.cubo.woolbrawl.listener;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.game.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Iterator;

public class TntListener implements Listener {

    private final WoolBrawlPlugin plugin;

    public TntListener(WoolBrawlPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (plugin.game().state() == GameState.IDLE) return;
        if (!(e.getEntity() instanceof TNTPrimed)) return;

        Iterator<Block> it = e.blockList().iterator();
        while (it.hasNext()) {
            Block b = it.next();
            if (!isBreakableWool(b.getType())) {
                it.remove();
            }
        }
        e.setYield(0f);

        Location center = e.getEntity().getLocation();
        double radius = 5.0;
        for (Player target : center.getWorld().getPlayers()) {
            if (plugin.game().data(target) == null) continue;
            if (target.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
            double dist = target.getLocation().distance(center);
            if (dist > radius) continue;

            double dmg = Math.max(0, 7.0 * (1.0 - dist / radius));
            if (dmg > 0) {
                target.damage(dmg);

                Vector kb = target.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.8);
                kb.setY(Math.max(0.3, kb.getY() + 0.3));
                target.setVelocity(target.getVelocity().add(kb));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTntPlace(BlockPlaceEvent e) {
        if (plugin.game().state() != GameState.PLAYING) return;
        if (e.getBlock().getType() != Material.TNT) return;
        if (plugin.game().data(e.getPlayer()) == null) return;

        Player p = e.getPlayer();
        Block b = e.getBlock();
        Location spawnLoc = b.getLocation().add(0.5, 0.0, 0.5);

        e.setCancelled(true);

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand.getType() == Material.TNT) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            ItemStack off = p.getInventory().getItemInOffHand();
            if (off.getType() == Material.TNT) off.setAmount(off.getAmount() - 1);
        }

        TNTPrimed tnt = b.getWorld().spawn(spawnLoc, TNTPrimed.class, prim -> {
            prim.setFuseTicks(40);

        });
        tnt.setVelocity(new Vector(0, 0, 0));

        p.playSound(p.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent e) {
        if (plugin.game().state() == GameState.IDLE) return;
        if (!(e.getEntity() instanceof ItemFrame)) return;

        if (e.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION
                || e.getCause() == HangingBreakEvent.RemoveCause.PHYSICS) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        if (plugin.game().state() == GameState.IDLE) return;
        if (!(e.getEntity() instanceof ItemFrame)) return;
        Entity remover = e.getRemover();

        if (remover instanceof Player pl && pl.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (plugin.game().state() == GameState.IDLE) return;
        if (!(e.getEntity() instanceof ItemFrame)) return;

        e.setCancelled(true);
    }

    private static boolean isBreakableWool(Material m) {
        if (m == Material.BLACK_WOOL) return false;
        return m == Material.WHITE_WOOL || m == Material.RED_WOOL || m == Material.BLUE_WOOL
                || m == Material.ORANGE_WOOL || m == Material.MAGENTA_WOOL
                || m == Material.LIGHT_BLUE_WOOL || m == Material.YELLOW_WOOL || m == Material.LIME_WOOL
                || m == Material.PINK_WOOL || m == Material.GRAY_WOOL || m == Material.LIGHT_GRAY_WOOL
                || m == Material.CYAN_WOOL || m == Material.PURPLE_WOOL || m == Material.BROWN_WOOL
                || m == Material.GREEN_WOOL;
    }
}
