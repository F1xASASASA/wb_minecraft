package net.cubo.woolbrawl.listener;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.game.GameState;
import net.cubo.woolbrawl.kit.KitSelectGUI;
import net.cubo.woolbrawl.game.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    private final WoolBrawlPlugin plugin;

    public InteractListener(WoolBrawlPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!KitSelectGUI.isOpener(e.getItem())) return;
        if (plugin.game().state() != GameState.LOBBY) return;

        PlayerData d = plugin.game().data(e.getPlayer());
        int cur = (d != null && d.kit() != null) ? d.kit().id() : -1;
        net.cubo.woolbrawl.game.Team team = (d != null) ? d.team() : null;

        KitSelectGUI.open(e.getPlayer(), plugin.game().kitPool(), cur, team);
        e.setCancelled(true);
    }
}
