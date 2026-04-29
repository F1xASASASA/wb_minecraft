package net.cubo.woolbrawl.listener;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.game.GameState;
import net.cubo.woolbrawl.game.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MiscListener implements Listener {

    private final WoolBrawlPlugin plugin;

    public MiscListener(WoolBrawlPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        if (plugin.game().state() != GameState.PLAYING) return;

        Player attacker = null;
        boolean isRanged = false;
        boolean isHealArrow = false;
        if (e.getDamager() instanceof Player p) {
            attacker = p;
            isRanged = false;
        } else if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
            isRanged = true;
            if (proj instanceof org.bukkit.entity.Arrow arrow) {
                org.bukkit.potion.PotionType base = arrow.getBasePotionType();
                if (base != null && base.name().contains("HEAL")) isHealArrow = true;
                for (org.bukkit.potion.PotionEffect eff : arrow.getCustomEffects()) {
                    if (eff.getType().equals(org.bukkit.potion.PotionEffectType.INSTANT_HEALTH)) {
                        isHealArrow = true;
                        break;
                    }
                }
            }
        }
        if (attacker == null) return;

        PlayerData a = plugin.game().data(attacker);
        PlayerData v = plugin.game().data(victim);

        if (a == null && v == null) return;

        if (a == null || v == null) {
            e.setCancelled(true);
            return;
        }

        if (a.team() == v.team()) {
            if (!isRanged || isHealArrow) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (plugin.game().data(p) == null) return;
        e.setCancelled(true);
        p.setFoodLevel(20);
        p.setSaturation(20f);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerData d = plugin.game().data(e.getPlayer());
        if (d == null) return;
        d.setAlive(false);
    }

    @EventHandler
    public void onProjectileHit(org.bukkit.event.entity.ProjectileHitEvent e) {
        if (plugin.game().state() != GameState.PLAYING) return;
        if (!(e.getEntity() instanceof org.bukkit.entity.AbstractArrow arrow)) return;

        if (!(arrow.getShooter() instanceof Player shooter) || plugin.game().data(shooter) == null) {
            return;
        }

        if (e.getHitEntity() instanceof Player victim
                && arrow instanceof org.bukkit.entity.Arrow tippedArrow) {
            boolean isHeal = false;
            org.bukkit.potion.PotionType base = tippedArrow.getBasePotionType();
            if (base != null && base.name().contains("HEAL")) isHeal = true;
            for (org.bukkit.potion.PotionEffect eff : tippedArrow.getCustomEffects()) {
                if (eff.getType().equals(org.bukkit.potion.PotionEffectType.INSTANT_HEALTH)) {
                    isHeal = true;
                    break;
                }
            }
            if (isHeal) {
                PlayerData s = plugin.game().data(shooter);
                PlayerData v = plugin.game().data(victim);
                if (s != null && v != null && s.team() == v.team()) {
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.INSTANT_HEALTH, 1, 1, true, false));
                    victim.getWorld().spawnParticle(org.bukkit.Particle.HEART,
                            victim.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.0);
                    victim.playSound(victim.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 2.0f);
                }
            }
        }

    }
}
