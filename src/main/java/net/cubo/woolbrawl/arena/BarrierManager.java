package net.cubo.woolbrawl.arena;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;

public class BarrierManager {

    private final WoolBrawlPlugin plugin;
    private final Map<Block, BlockData> placed = new LinkedHashMap<>();
    private static final Material BARRIER_BLOCK = Material.RED_STAINED_GLASS;

    public BarrierManager(WoolBrawlPlugin plugin) { this.plugin = plugin; }

    public void build() {
        clear();
        buildBox(plugin.cfg().barrierRedMin(), plugin.cfg().barrierRedMax());
        buildBox(plugin.cfg().barrierBlueMin(), plugin.cfg().barrierBlueMax());
    }

    private void buildBox(Vector min, Vector max) {

        if (min.getX() == 0 && min.getY() == 0 && min.getZ() == 0
                && max.getX() == 0 && max.getY() == 0 && max.getZ() == 0) {
            return;
        }
        World w = plugin.cfg().world();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Block b = w.getBlockAt(x, y, z);
                    if (!b.getType().isAir()) continue;
                    placed.put(b, b.getBlockData().clone());
                    b.setType(BARRIER_BLOCK, false);
                }
            }
        }
    }

    public void clear() {
        for (Map.Entry<Block, BlockData> e : placed.entrySet()) {
            Block b = e.getKey();
            if (b.getType() == BARRIER_BLOCK) {
                b.setBlockData(e.getValue(), false);
            }
        }
        placed.clear();
    }

    public boolean isActive() { return !placed.isEmpty(); }
}
