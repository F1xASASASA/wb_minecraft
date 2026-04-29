package net.cubo.woolbrawl.config;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

public class WbConfig {

    private final WoolBrawlPlugin plugin;
    private World world;

    private Location lobby, spawnRed, spawnBlue, spectator;

    private Vector zoneMin, zoneMax;
    private Vector arenaMin, arenaMax;

    private int lobbyTime, unlockTime, roundTime, endingTime;
    private int minPlayers;

    private Vector barrierRedMin, barrierRedMax;
    private Vector barrierBlueMin, barrierBlueMax;

    private String theme;
    private float themeVolume;

    public WbConfig(WoolBrawlPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        boolean dirty = false;
        if (!cfg.contains("theme"))         { cfg.set("theme", "battlebox:theme"); dirty = true; }
        if (!cfg.contains("theme-volume"))  { cfg.set("theme-volume", 1.0); dirty = true; }
        if (!cfg.contains("barriers.red.min"))  { cfg.set("barriers.red.min.x", 0); cfg.set("barriers.red.min.y", 0); cfg.set("barriers.red.min.z", 0); dirty = true; }
        if (!cfg.contains("barriers.red.max"))  { cfg.set("barriers.red.max.x", 0); cfg.set("barriers.red.max.y", 0); cfg.set("barriers.red.max.z", 0); dirty = true; }
        if (!cfg.contains("barriers.blue.min")) { cfg.set("barriers.blue.min.x", 0); cfg.set("barriers.blue.min.y", 0); cfg.set("barriers.blue.min.z", 0); dirty = true; }
        if (!cfg.contains("barriers.blue.max")) { cfg.set("barriers.blue.max.x", 0); cfg.set("barriers.blue.max.y", 0); cfg.set("barriers.blue.max.z", 0); dirty = true; }
        if (dirty) plugin.saveConfig();

        String worldName = cfg.getString("world", "world");
        this.world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Мир '" + worldName + "' не найден! Использую первый доступный.");
            if (!Bukkit.getWorlds().isEmpty()) this.world = Bukkit.getWorlds().get(0);
        }

        this.lobby = loadLoc(cfg, "points.lobby");
        this.spawnRed = loadLoc(cfg, "points.spawn-red");
        this.spawnBlue = loadLoc(cfg, "points.spawn-blue");
        this.spectator = loadLoc(cfg, "points.spectator");

        this.zoneMin = loadVec(cfg, "zone.min");
        this.zoneMax = loadVec(cfg, "zone.max");
        normalize(zoneMin, zoneMax);

        this.arenaMin = loadVec(cfg, "arena.min");
        this.arenaMax = loadVec(cfg, "arena.max");
        normalize(arenaMin, arenaMax);

        this.barrierRedMin = loadVec(cfg, "barriers.red.min");
        this.barrierRedMax = loadVec(cfg, "barriers.red.max");
        normalize(barrierRedMin, barrierRedMax);
        this.barrierBlueMin = loadVec(cfg, "barriers.blue.min");
        this.barrierBlueMax = loadVec(cfg, "barriers.blue.max");
        normalize(barrierBlueMin, barrierBlueMax);

        this.lobbyTime = cfg.getInt("timings.lobby", 19);
        this.unlockTime = cfg.getInt("timings.unlock", 12);
        this.roundTime = cfg.getInt("timings.round", 62);
        this.endingTime = cfg.getInt("timings.ending", 12);
        this.minPlayers = cfg.getInt("min-players", 1);

        this.theme = cfg.getString("theme", "battlebox:theme");
        double v = cfg.getDouble("theme-volume", 1.0);
        this.themeVolume = (float) Math.max(0.0, Math.min(1.0, v));
    }

    private Location loadLoc(FileConfiguration cfg, String path) {
        ConfigurationSection s = cfg.getConfigurationSection(path);
        if (s == null) return new Location(world, 0, 65, 0);
        return new Location(world,
                s.getDouble("x"), s.getDouble("y"), s.getDouble("z"),
                (float) s.getDouble("yaw"), (float) s.getDouble("pitch"));
    }

    private Vector loadVec(FileConfiguration cfg, String path) {
        ConfigurationSection s = cfg.getConfigurationSection(path);
        if (s == null) return new Vector(0, 0, 0);
        return new Vector(s.getInt("x"), s.getInt("y"), s.getInt("z"));
    }

    private void normalize(Vector a, Vector b) {
        double minX = Math.min(a.getX(), b.getX()), maxX = Math.max(a.getX(), b.getX());
        double minY = Math.min(a.getY(), b.getY()), maxY = Math.max(a.getY(), b.getY());
        double minZ = Math.min(a.getZ(), b.getZ()), maxZ = Math.max(a.getZ(), b.getZ());
        a.setX(minX); a.setY(minY); a.setZ(minZ);
        b.setX(maxX); b.setY(maxY); b.setZ(maxZ);
    }

    public void setLocation(String key, Location loc) {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("points." + key + ".x", loc.getX());
        cfg.set("points." + key + ".y", loc.getY());
        cfg.set("points." + key + ".z", loc.getZ());
        cfg.set("points." + key + ".yaw", loc.getYaw());
        cfg.set("points." + key + ".pitch", loc.getPitch());
        plugin.saveConfig();
        reload();
    }

    public void setCorner(String zoneKey, String corner, Vector v) {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set(zoneKey + "." + corner + ".x", v.getBlockX());
        cfg.set(zoneKey + "." + corner + ".y", v.getBlockY());
        cfg.set(zoneKey + "." + corner + ".z", v.getBlockZ());
        plugin.saveConfig();
        reload();
    }

    public World world() { return world; }
    public Location lobby() { return lobby.clone(); }
    public Location spawnRed() { return spawnRed.clone(); }
    public Location spawnBlue() { return spawnBlue.clone(); }
    public Location spectator() { return spectator.clone(); }
    public Vector zoneMin() { return zoneMin.clone(); }
    public Vector zoneMax() { return zoneMax.clone(); }
    public Vector arenaMin() { return arenaMin.clone(); }
    public Vector arenaMax() { return arenaMax.clone(); }
    public int lobbyTime() { return lobbyTime; }
    public int unlockTime() { return unlockTime; }
    public int roundTime() { return roundTime; }
    public int endingTime() { return endingTime; }
    public int minPlayers() { return minPlayers; }

    public Vector barrierRedMin() { return barrierRedMin.clone(); }
    public Vector barrierRedMax() { return barrierRedMax.clone(); }
    public Vector barrierBlueMin() { return barrierBlueMin.clone(); }
    public Vector barrierBlueMax() { return barrierBlueMax.clone(); }

    public String theme() { return theme; }
    public float themeVolume() { return themeVolume; }

    public boolean inZone(Location l) { return inBox(l, zoneMin, zoneMax); }
    public boolean inArena(Location l) { return inBox(l, arenaMin, arenaMax); }

    private boolean inBox(Location l, Vector min, Vector max) {
        if (!l.getWorld().equals(world)) return false;
        double x = l.getX(), y = l.getY(), z = l.getZ();
        return x >= min.getX() && x <= max.getX() + 1
            && y >= min.getY() && y <= max.getY() + 1
            && z >= min.getZ() && z <= max.getZ() + 1;
    }
}
