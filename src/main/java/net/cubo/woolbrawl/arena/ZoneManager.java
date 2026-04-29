package net.cubo.woolbrawl.arena;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.game.Team;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ZoneManager {

    private final WoolBrawlPlugin plugin;

    public ZoneManager(WoolBrawlPlugin plugin) {
        this.plugin = plugin;
    }

    public List<Block> zoneBlocks() {
        List<Block> list = new ArrayList<>();
        World w = plugin.cfg().world();
        Vector min = plugin.cfg().zoneMin();
        Vector max = plugin.cfg().zoneMax();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    list.add(w.getBlockAt(x, y, z));
                }
            }
        }
        return list;
    }

    public void fill(Material m) {
        for (Block b : zoneBlocks()) b.setType(m, false);
    }

    public void lock() {
        fill(Material.BLACK_WOOL);
    }

    public void unlock() {
        for (Block b : zoneBlocks()) {
            if (b.getType() == Material.BLACK_WOOL) {
                b.setType(Material.WHITE_WOOL, false);
            }
        }
    }

    public void resetWhite() {
        fill(Material.WHITE_WOOL);
    }

    public int count(Material m) {
        int c = 0;
        for (Block b : zoneBlocks()) if (b.getType() == m) c++;
        return c;
    }

    public int size() {
        Vector min = plugin.cfg().zoneMin();
        Vector max = plugin.cfg().zoneMax();
        return (max.getBlockX() - min.getBlockX() + 1)
             * (max.getBlockY() - min.getBlockY() + 1)
             * (max.getBlockZ() - min.getBlockZ() + 1);
    }

    public boolean claim(Block b, Team team) {
        if (!plugin.cfg().inZone(b.getLocation())) return false;
        if (b.getType() != Material.AIR) return false;
        b.setType(team.wool(), false);
        return true;
    }

    public boolean isInZone(Block b) {
        if (!b.getWorld().equals(plugin.cfg().world())) return false;
        Vector min = plugin.cfg().zoneMin();
        Vector max = plugin.cfg().zoneMax();
        int x = b.getX(), y = b.getY(), z = b.getZ();
        return x >= min.getBlockX() && x <= max.getBlockX()
            && y >= min.getBlockY() && y <= max.getBlockY()
            && z >= min.getBlockZ() && z <= max.getBlockZ();
    }
}
