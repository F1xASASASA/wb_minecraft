package net.cubo.woolbrawl.command;

import net.cubo.woolbrawl.WoolBrawlPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WbCommand implements CommandExecutor {

    private final WoolBrawlPlugin plugin;

    public WbCommand(WoolBrawlPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (args.length == 0) { help(s); return true; }

        switch (args[0].toLowerCase()) {
            case "help" -> help(s);
            case "start" -> {
                if (!(s instanceof Player p)) { s.sendMessage("Только игрок."); return true; }
                plugin.game().start(p);
            }
            case "stop" -> plugin.game().stop();
            case "reload" -> {
                plugin.cfg().reload();
                s.sendMessage(Component.text("Конфиг перезагружен.").color(NamedTextColor.GREEN));
            }
            case "setpoint" -> setpoint(s, args);
            case "setzone" -> setzone(s, args, "zone");
            case "setarena" -> setzone(s, args, "arena");
            case "setbarrier" -> setbarrier(s, args);
            case "show" -> show(s);
            default -> help(s);
        }
        return true;
    }

    private void setbarrier(CommandSender s, String[] args) {
        if (!(s instanceof Player p)) { s.sendMessage("Только игрок."); return; }
        if (args.length < 3) {
            s.sendMessage("Укажи: /wb setbarrier <red|blue> <min|max>");
            return;
        }
        String team = args[1].toLowerCase();
        String corner = args[2].toLowerCase();
        if (!team.equals("red") && !team.equals("blue")) {
            s.sendMessage("team должен быть red или blue");
            return;
        }
        if (!corner.equals("min") && !corner.equals("max")) {
            s.sendMessage("corner должен быть min или max");
            return;
        }
        Vector v = new Vector(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
        plugin.cfg().setCorner("barriers." + team, corner, v);
        p.sendMessage(Component.text("barriers." + team + "." + corner + " = "
                + v.getBlockX() + "," + v.getBlockY() + "," + v.getBlockZ()).color(NamedTextColor.GREEN));
    }

    private void setpoint(CommandSender s, String[] args) {
        if (!(s instanceof Player p)) { s.sendMessage("Только игрок."); return; }
        if (args.length < 2) {
            s.sendMessage("Укажи: lobby | spawn-red | spawn-blue | spectator");
            return;
        }
        String key = args[1].toLowerCase();
        if (!key.equals("lobby") && !key.equals("spawn-red") && !key.equals("spawn-blue") && !key.equals("spectator")) {
            s.sendMessage("Неизвестная точка: " + key);
            return;
        }
        plugin.cfg().setLocation(key, p.getLocation());
        p.sendMessage(Component.text("Точка " + key + " установлена.").color(NamedTextColor.GREEN));
    }

    private void setzone(CommandSender s, String[] args, String zoneKey) {
        if (!(s instanceof Player p)) { s.sendMessage("Только игрок."); return; }
        if (args.length < 2) {
            s.sendMessage("Укажи: min | max");
            return;
        }
        String corner = args[1].toLowerCase();
        if (!corner.equals("min") && !corner.equals("max")) {
            s.sendMessage("corner должен быть min или max");
            return;
        }
        Vector v = new Vector(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
        plugin.cfg().setCorner(zoneKey, corner, v);
        p.sendMessage(Component.text(zoneKey + "." + corner + " = " + v.getBlockX() + "," + v.getBlockY() + "," + v.getBlockZ()).color(NamedTextColor.GREEN));
    }

    private void show(CommandSender s) {
        s.sendMessage(Component.text("=== КОНФИГ WB ===").color(NamedTextColor.GOLD));
        s.sendMessage("Лобби: " + fmt(plugin.cfg().lobby()));
        s.sendMessage("Красные: " + fmt(plugin.cfg().spawnRed()));
        s.sendMessage("Синие: " + fmt(plugin.cfg().spawnBlue()));
        s.sendMessage("Зрители: " + fmt(plugin.cfg().spectator()));
        s.sendMessage("Зона: " + plugin.cfg().zoneMin() + " → " + plugin.cfg().zoneMax());
        s.sendMessage("Арена: " + plugin.cfg().arenaMin() + " → " + plugin.cfg().arenaMax());
        s.sendMessage("Барьер RED: " + plugin.cfg().barrierRedMin() + " → " + plugin.cfg().barrierRedMax());
        s.sendMessage("Барьер BLUE: " + plugin.cfg().barrierBlueMin() + " → " + plugin.cfg().barrierBlueMax());
        s.sendMessage("Тайминги: lobby=" + plugin.cfg().lobbyTime() + ", unlock=" + plugin.cfg().unlockTime()
                + ", round=" + plugin.cfg().roundTime() + ", end=" + plugin.cfg().endingTime());
        s.sendMessage("Тема: '" + plugin.cfg().theme() + "', volume=" + plugin.cfg().themeVolume());
    }

    private String fmt(org.bukkit.Location l) {
        return String.format("%.1f, %.1f, %.1f (yaw=%.0f)", l.getX(), l.getY(), l.getZ(), l.getYaw());
    }

    private void help(CommandSender s) {
        s.sendMessage(Component.text("=== WoolBrawl ===").color(NamedTextColor.GOLD));
        s.sendMessage("/wb start - запустить игру");
        s.sendMessage("/wb stop - остановить");
        s.sendMessage("/wb reload - перезагрузить конфиг");
        s.sendMessage("/wb show - показать конфиг");
        s.sendMessage("/wb setpoint <lobby|spawn-red|spawn-blue|spectator>");
        s.sendMessage("/wb setzone <min|max> - угол зоны захвата");
        s.sendMessage("/wb setarena <min|max> - угол арены (ТНТ)");
        s.sendMessage("/wb setbarrier <red|blue> <min|max> - угол барьера команды (preroll)");
    }
}
