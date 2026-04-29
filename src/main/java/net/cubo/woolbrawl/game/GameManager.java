package net.cubo.woolbrawl.game;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.cubo.woolbrawl.arena.ZoneManager;
import net.cubo.woolbrawl.kit.Kit;
import net.cubo.woolbrawl.kit.KitRegistry;
import net.cubo.woolbrawl.kit.KitSelectGUI;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;

public class GameManager {

    private final WoolBrawlPlugin plugin;
    private final ZoneManager zone;
    private final net.cubo.woolbrawl.arena.BarrierManager barriers;

    private GameState state = GameState.IDLE;
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private List<Kit> kitPool;

    private int redStartSize, blueStartSize;

    private final java.util.Set<UUID> redStartRoster = new java.util.HashSet<>();
    private final java.util.Set<UUID> blueStartRoster = new java.util.HashSet<>();

    private final Map<UUID, java.util.Set<UUID>> killsByPlayer = new HashMap<>();

    private final java.util.Set<UUID> aceAnnounced = new java.util.HashSet<>();

    private BukkitTask task;
    private int ticksLeft;
    private int unlockTicksLeft;

    private final BossBar timerBar = BossBar.bossBar(Component.text("Таймер"), 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
    private final BossBar redBar = BossBar.bossBar(Component.text("Красные 0/9"), 0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    private final BossBar blueBar = BossBar.bossBar(Component.text("Синие 0/9"), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);

    public GameManager(WoolBrawlPlugin plugin) {
        this.plugin = plugin;
        this.zone = new ZoneManager(plugin);
        this.barriers = new net.cubo.woolbrawl.arena.BarrierManager(plugin);
    }

    private String currentTheme = null;

    public GameState state() { return state; }
    public ZoneManager zone() { return zone; }
    public boolean isRunning() { return state != GameState.IDLE; }
    public PlayerData data(Player p) { return players.get(p.getUniqueId()); }
    public Collection<PlayerData> allData() { return players.values(); }

    public List<Player> online(Team team) {
        List<Player> list = new ArrayList<>();
        for (PlayerData d : players.values()) {
            if (d.team() == team) {
                Player p = Bukkit.getPlayer(d.uuid());
                if (p != null) list.add(p);
            }
        }
        return list;
    }

    public int aliveCount(Team team) {
        int c = 0;
        for (PlayerData d : players.values()) {
            if (d.team() == team && d.alive()) c++;
        }
        return c;
    }

    public boolean start(Player admin) {
        if (state != GameState.IDLE) {
            admin.sendMessage(Component.text("Игра уже идёт. /wb stop").color(NamedTextColor.RED));
            return false;
        }

        Set<Player> participants = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() != GameMode.SPECTATOR) {
                participants.add(p);
            }
        }

        if (participants.size() < plugin.cfg().minPlayers()) {
            admin.sendMessage(Component.text("Нужно минимум " + plugin.cfg().minPlayers() + " игроков.").color(NamedTextColor.RED));
            return false;
        }

        kitPool = plugin.kits().roll(6);

        players.clear();
        List<Player> shuffled = new ArrayList<>(participants);
        Collections.shuffle(shuffled);
        for (int i = 0; i < shuffled.size(); i++) {
            Player p = shuffled.get(i);
            PlayerData d = new PlayerData(p.getUniqueId());
            d.setTeam(i % 2 == 0 ? Team.RED : Team.BLUE);
            d.setAlive(true);
            players.put(p.getUniqueId(), d);
        }
        redStartSize = (int) players.values().stream().filter(d -> d.team() == Team.RED).count();
        blueStartSize = (int) players.values().stream().filter(d -> d.team() == Team.BLUE).count();

        redStartRoster.clear();
        blueStartRoster.clear();
        killsByPlayer.clear();
        aceAnnounced.clear();
        for (PlayerData d : players.values()) {
            if (d.team() == Team.RED) redStartRoster.add(d.uuid());
            else blueStartRoster.add(d.uuid());
        }

        zone.lock();

        for (Player p : participants) {
            prepPlayer(p);
            p.teleport(plugin.cfg().lobby());
            PlayerInventory inv = p.getInventory();
            inv.clear();
            inv.setItem(4, KitSelectGUI.opener());
            p.sendMessage(Component.text("§eПКМ по изумруду — выбери кит!"));
        }

        for (Player p : participants) {
            p.showBossBar(timerBar);
        }

        setState(GameState.LOBBY);
        ticksLeft = plugin.cfg().lobbyTime() * 20;
        startTicker();

        broadcast(Component.text("§c§l⚔ ШЕРСТЯНАЯ ПОТАСОВКА ⚔"));
        broadcast(Component.text("§7Выбери кит за " + plugin.cfg().lobbyTime() + " секунд."));

        for (Player p : participants) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            p.showTitle(Title.title(
                    Component.text("Выбор кита").color(NamedTextColor.YELLOW),
                    Component.text("ПКМ по изумруду"),
                    Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(500))
            ));
        }

        playTheme();

        return true;
    }

    private void playTheme() {
        stopTheme();
        String pick = plugin.cfg().theme();
        if (pick == null || pick.isBlank()) return;
        currentTheme = pick;
        float vol = plugin.cfg().themeVolume();
        for (PlayerData d : players.values()) {
            Player p = Bukkit.getPlayer(d.uuid());
            if (p == null) continue;
            try {
                p.playSound(p.getLocation(), pick, org.bukkit.SoundCategory.RECORDS, vol, 1f);
            } catch (Throwable t) {
                plugin.getLogger().warning("Не удалось проиграть тему '" + pick + "': " + t.getMessage());
            }
        }
    }

    private void stopTheme() {
        if (currentTheme == null) return;
        String key = currentTheme;
        currentTheme = null;
        for (PlayerData d : players.values()) {
            Player p = Bukkit.getPlayer(d.uuid());
            if (p == null) continue;
            try {
                p.stopSound(key, org.bukkit.SoundCategory.RECORDS);
            } catch (Throwable ignored) {}
        }
    }

    private void prepPlayer(Player p) {
        p.setGameMode(GameMode.ADVENTURE);

        org.bukkit.attribute.AttributeInstance maxHp = p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (maxHp != null) maxHp.setBaseValue(maxHp.getDefaultValue());
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setFireTicks(0);
        p.setFallDistance(0f);
        p.setExp(0f);
        p.setLevel(0);
        for (PotionEffect e : p.getActivePotionEffects()) p.removePotionEffect(e.getType());
        p.getInventory().clear();
    }

    public void stop() {
        if (state == GameState.IDLE) return;
        if (task != null) task.cancel();

        stopTheme();

        for (PlayerData d : players.values()) {
            Player p = Bukkit.getPlayer(d.uuid());
            if (p == null) continue;
            p.hideBossBar(timerBar);
            p.hideBossBar(redBar);
            p.hideBossBar(blueBar);
            p.setGameMode(GameMode.ADVENTURE);
            for (PotionEffect e : p.getActivePotionEffects()) p.removePotionEffect(e.getType());
            p.getInventory().clear();
            p.setAllowFlight(false);
            p.setFlying(false);
            p.teleport(plugin.cfg().lobby());
        }

        zone.resetWhite();

        barriers.clear();

        ScoreboardHelper.clearAll();

        players.clear();
        kitPool = null;
        setState(GameState.IDLE);

        broadcast(Component.text("§7Игра завершена."));
    }

    private void equipKitFully(Player p, PlayerData d) {
        if (d.kit() == null) return;
        p.getInventory().clear();
        d.kit().equip(p.getInventory());
        KitRegistry.dyeLeather(p.getInventory(), d.team().armorColor());

        ItemStack wool = new ItemStack(d.team().wool(), 64);
        ItemStack shears = new ItemStack(Material.SHEARS);
        p.getInventory().setItem(7, wool);
        p.getInventory().setItem(8, shears);

        p.getInventory().setHeldItemSlot(0);

        p.updateInventory();
    }

    private void beginPlaying() {

        resolveKitConflicts();

        for (PlayerData d : players.values()) {
            Player p = Bukkit.getPlayer(d.uuid());
            if (p == null) continue;
            if (d.kit() == null) d.setKit(kitPool.get(0));

            for (PotionEffect eff : p.getActivePotionEffects()) p.removePotionEffect(eff.getType());
            org.bukkit.attribute.AttributeInstance maxHp = p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (maxHp != null) maxHp.setBaseValue(maxHp.getDefaultValue());
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.setFireTicks(0);
            p.setFallDistance(0f);
            p.setExp(0f);
            p.setLevel(0);
            p.setAllowFlight(false);
            p.setFlying(false);

            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(d.team() == Team.RED ? plugin.cfg().spawnRed() : plugin.cfg().spawnBlue());

            ScoreboardHelper.assign(p, d.team());

            final PlayerData fd = d;
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player pl = Bukkit.getPlayer(fd.uuid());
                if (pl == null) return;
                equipKitFully(pl, fd);
            }, 1L);
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player pl = Bukkit.getPlayer(fd.uuid());
                if (pl == null) return;
                ItemStack slot0 = pl.getInventory().getItem(0);
                if (slot0 == null || slot0.getType() == Material.AIR) {
                    equipKitFully(pl, fd);
                }
            }, 5L);

            p.showTitle(Title.title(
                    Component.text("ПРИГОТОВЬТЕСЬ!").color(NamedTextColor.GOLD),
                    Component.text("Старт через " + plugin.cfg().unlockTime() + " сек"),
                    Title.Times.times(Duration.ofMillis(150), Duration.ofSeconds(2), Duration.ofMillis(500))
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.5f);
        }

        barriers.build();

        setState(GameState.PLAYING);
        ticksLeft = plugin.cfg().roundTime() * 20;
        unlockTicksLeft = plugin.cfg().unlockTime() * 20;
    }

    private void startTicker() {
        if (task != null) task.cancel();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void tick() {
        switch (state) {
            case LOBBY -> tickLobby();
            case PLAYING -> tickPlaying();
            case ENDING -> tickEnding();
            default -> {}
        }
    }

    private void tickLobby() {
        ticksLeft--;
        float progress = Math.max(0f, (float) ticksLeft / (plugin.cfg().lobbyTime() * 20f));
        timerBar.progress(Math.min(1f, progress));
        int sec = (ticksLeft + 19) / 20;
        timerBar.name(Component.text("Выбор кита: " + sec + "с").color(NamedTextColor.YELLOW));

        if (ticksLeft % 20 == 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (players.containsKey(p.getUniqueId())) {
                    p.setFoodLevel(20);
                    p.setSaturation(20f);
                    p.setHealth(p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
                    if (sec <= 5 && sec >= 1) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1.2f);
                    }
                }
            }
        }

        if (ticksLeft <= 0) beginPlaying();
    }

    private void tickPlaying() {

        if (unlockTicksLeft > 0) {
            unlockTicksLeft--;

            if (unlockTicksLeft % 20 == 0) {
                for (PlayerData d : players.values()) {
                    Player p = Bukkit.getPlayer(d.uuid());
                    if (p != null) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1.5f);
                }
            }
            int prerollSec = (unlockTicksLeft + 19) / 20;
            timerBar.progress(Math.min(1f, (float) unlockTicksLeft / (plugin.cfg().unlockTime() * 20f)));
            timerBar.name(Component.text("⏳ Старт через " + prerollSec + "с").color(NamedTextColor.GOLD));
            timerBar.color(BossBar.Color.YELLOW);

            if (unlockTicksLeft == 0) {

                barriers.clear();

                zone.unlock();

                for (PlayerData d : players.values()) {
                    Player p = Bukkit.getPlayer(d.uuid());
                    if (p == null) continue;
                    p.showTitle(Title.title(
                            Component.text("В БОЙ!").color(NamedTextColor.GREEN),
                            Component.empty(),
                            Title.Times.times(Duration.ofMillis(150), Duration.ofMillis(800), Duration.ofMillis(250))
                    ));
                    p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1.5f);
                }
                broadcastParticipants(Component.text("§e§lЦентр открыт!"));
            }
            return;
        }

        ticksLeft--;

        if (ticksLeft % 5 == 0) {
            voidCheck();
        }

        float progress = Math.max(0f, (float) ticksLeft / (plugin.cfg().roundTime() * 20f));
        timerBar.progress(Math.min(1f, progress));
        int sec = (ticksLeft + 19) / 20;
        timerBar.name(Component.text("⏱ " + sec + "с").color(NamedTextColor.WHITE));
        timerBar.color(sec <= 10 ? BossBar.Color.RED : BossBar.Color.WHITE);

        if (ticksLeft % 5 == 0) {
            int red = zone.count(Material.RED_WOOL);
            int blue = zone.count(Material.BLUE_WOOL);
            int total = zone.size();

            checkWin(red, blue, total);
        }

        if (sec <= 10 && sec >= 1 && ticksLeft % 20 == 0) {
            for (PlayerData d : players.values()) {
                Player p = Bukkit.getPlayer(d.uuid());
                if (p != null) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 2f);
            }
        }

        if (ticksLeft <= 0) timeUp();
    }

    private void tickEnding() {
        ticksLeft--;
        if (ticksLeft <= 0) stop();
    }

    private void voidCheck() {
        double limitRed = plugin.cfg().spawnRed().getY() - 8.0;
        double limitBlue = plugin.cfg().spawnBlue().getY() - 8.0;
        for (PlayerData d : players.values()) {
            if (!d.alive()) continue;
            Player p = Bukkit.getPlayer(d.uuid());
            if (p == null) continue;
            if (p.getGameMode() == GameMode.SPECTATOR) continue;
            double y = p.getLocation().getY();
            double limit = d.team() == Team.RED ? limitRed : limitBlue;
            if (y < limit) {

                p.setHealth(0.0);
            }
        }
    }

    private void checkWin(int red, int blue, int total) {

        if (red >= total) { endWith(Team.RED, "Захват!"); return; }
        if (blue >= total) { endWith(Team.BLUE, "Захват!"); return; }

    }

    private void timeUp() {
        int red = zone.count(Material.RED_WOOL);
        int blue = zone.count(Material.BLUE_WOOL);
        if (red > blue) endWith(Team.RED, "По очкам");
        else if (blue > red) endWith(Team.BLUE, "По очкам");
        else endDraw();
    }

    private void endWith(Team winner, String reason) {
        setState(GameState.ENDING);
        ticksLeft = plugin.cfg().endingTime() * 20;
        timerBar.name(Component.text("Игра окончена").color(NamedTextColor.GOLD));
        timerBar.progress(1f);

        Component title = Component.text(winner.display() + " ПОБЕДИЛИ!").color(winner.chatColor());
        Component sub = Component.text(reason).color(NamedTextColor.GRAY);
        for (PlayerData d : players.values()) {
            Player p = Bukkit.getPlayer(d.uuid());
            if (p == null) continue;
            p.showTitle(Title.title(title, sub, Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            grantEndingFlight(p);
        }
        broadcastParticipants(Component.text("\n        " + winner.display() + " § §lПОБЕДА\n"));
    }

    private void endDraw() {
        setState(GameState.ENDING);
        ticksLeft = plugin.cfg().endingTime() * 20;
        for (PlayerData d : players.values()) {
            Player p = Bukkit.getPlayer(d.uuid());
            if (p == null) continue;
            p.showTitle(Title.title(Component.text("НИЧЬЯ").color(NamedTextColor.YELLOW),
                    Component.empty(), Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))));
            grantEndingFlight(p);
        }
        broadcastParticipants(Component.text("§e§lНИЧЬЯ"));
    }

    private void grantEndingFlight(Player p) {
        for (PotionEffect e : p.getActivePotionEffects()) p.removePotionEffect(e.getType());

        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, plugin.cfg().endingTime() * 20 + 40, 4, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, plugin.cfg().endingTime() * 20 + 40, 5, true, false));
        p.setFireTicks(0);
        p.setFallDistance(0f);

        p.setAllowFlight(true);
        p.setFlying(true);
    }

    public void onPlayerDeath(Player p, Player killer) {
        PlayerData d = data(p);
        if (d == null) return;
        d.setAlive(false);
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(plugin.cfg().spectator());
        broadcastParticipants(Component.text(p.getName() + " выбыл").color(NamedTextColor.GRAY));

        if (killer != null) {
            PlayerData kd = data(killer);
            if (kd != null && kd.team() != d.team()) {
                killsByPlayer
                        .computeIfAbsent(killer.getUniqueId(), k -> new java.util.HashSet<>())
                        .add(p.getUniqueId());
                checkAce(killer);
            }
        }
    }

    public void onPlayerDeath(Player p) { onPlayerDeath(p, null); }

    private void checkAce(Player killer) {
        if (aceAnnounced.contains(killer.getUniqueId())) return;
        PlayerData kd = data(killer);
        if (kd == null) return;
        java.util.Set<UUID> enemyRoster = (kd.team() == Team.RED) ? blueStartRoster : redStartRoster;
        if (enemyRoster.isEmpty()) return;

        java.util.Set<UUID> kills = killsByPlayer.getOrDefault(killer.getUniqueId(), java.util.Set.of());
        if (kills.containsAll(enemyRoster)) {
            aceAnnounced.add(killer.getUniqueId());
            announceAce(killer);
        }
    }

    private void announceAce(Player killer) {
        Component line1 = Component.text("§c§l⚔ ⚔ ⚔");
        Component line2 = Component.text("§6§lЭЙС!!!");
        Component line3 = Component.text("§eИгрок §f§l" + killer.getName() + " §eуничтожил всю вражескую команду!");

        for (PlayerData d : players.values()) {
            Player p = Bukkit.getPlayer(d.uuid());
            if (p == null) continue;
            p.sendMessage(Component.empty());
            p.sendMessage(line1);
            p.sendMessage(line2);
            p.sendMessage(line3);
            p.sendMessage(Component.empty());
            p.showTitle(Title.title(
                    Component.text("§6§l⚔ ЭЙС ⚔"),
                    Component.text("§f" + killer.getName() + " §7убил всех врагов"),
                    Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofSeconds(1))
            ));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.3f);
        }
    }

    private void resolveKitConflicts() {
        for (Team team : Team.values()) {
            java.util.Set<Integer> taken = new java.util.HashSet<>();
            for (PlayerData d : players.values()) {
                if (d.team() != team) continue;
                Kit k = d.kit();
                if (k == null) {

                    Kit free = firstFreeKit(team);
                    d.setKit(free);
                    if (free != null) taken.add(free.id());
                } else if (taken.contains(k.id())) {

                    Kit free = firstFreeKit(team);
                    d.setKit(free);
                    if (free != null) taken.add(free.id());
                } else {
                    taken.add(k.id());
                }
            }
        }
    }

    private Kit firstFreeKit(Team team) {
        if (kitPool == null) return null;
        java.util.Set<Integer> taken = new java.util.HashSet<>();
        for (PlayerData d : players.values()) {
            if (d.team() == team && d.kit() != null) taken.add(d.kit().id());
        }
        for (Kit k : kitPool) {
            if (!taken.contains(k.id())) return k;
        }
        return kitPool.get(0);
    }

    private void setState(GameState s) { this.state = s; }

    public java.util.UUID kitTakenBy(int kitId, Team team) {
        for (PlayerData d : players.values()) {
            if (d.team() == team && d.kit() != null && d.kit().id() == kitId) {
                return d.uuid();
            }
        }
        return null;
    }

    public String kitTakenByName(int kitId, Team team) {
        java.util.UUID uuid = kitTakenBy(kitId, team);
        if (uuid == null) return null;
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getName() : null;
    }

    public void broadcast(Component c) {
        Bukkit.broadcast(c);
    }

    public void broadcastParticipants(Component c) {
        for (PlayerData d : players.values()) {
            Player p = Bukkit.getPlayer(d.uuid());
            if (p != null) p.sendMessage(c);
        }
    }

    public List<Kit> kitPool() { return kitPool; }
}
