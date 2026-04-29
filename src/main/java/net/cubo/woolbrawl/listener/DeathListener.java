package net.cubo.woolbrawl.listener;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathListener implements Listener {

    private final WoolBrawlPlugin plugin;

    public DeathListener(WoolBrawlPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (plugin.game().data(p) == null) return;
        if (plugin.game().state() != GameState.PLAYING) return;
        e.setCancelled(false);
        e.getDrops().clear();
        e.setDroppedExp(0);
        final Player killer = e.getEntity().getKiller();
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.game().onPlayerDeath(p, killer);
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (plugin.game().data(e.getPlayer()) == null) return;
        e.setRespawnLocation(plugin.cfg().spectator());
    }
}
