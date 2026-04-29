package net.cubo.woolbrawl.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardHelper {

    private static final String TEAM_RED = "wb_red";
    private static final String TEAM_BLUE = "wb_blue";

    public static void ensureTeams() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        if (sb.getTeam(TEAM_RED) == null) {
            org.bukkit.scoreboard.Team t = sb.registerNewTeam(TEAM_RED);
            t.color(NamedTextColor.RED);
            t.prefix(Component.text("").color(NamedTextColor.RED));
            t.setAllowFriendlyFire(true);
            t.setCanSeeFriendlyInvisibles(true);
        }
        if (sb.getTeam(TEAM_BLUE) == null) {
            org.bukkit.scoreboard.Team t = sb.registerNewTeam(TEAM_BLUE);
            t.color(NamedTextColor.BLUE);
            t.prefix(Component.text("").color(NamedTextColor.BLUE));
            t.setAllowFriendlyFire(true);
            t.setCanSeeFriendlyInvisibles(true);
        }
    }

    public static void assign(Player p, Team team) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        org.bukkit.scoreboard.Team rt = sb.getTeam(TEAM_RED);
        org.bukkit.scoreboard.Team bt = sb.getTeam(TEAM_BLUE);
        if (rt != null) rt.removePlayer(p);
        if (bt != null) bt.removePlayer(p);

        org.bukkit.scoreboard.Team target = team == Team.RED ? rt : bt;
        if (target != null) target.addPlayer(p);
    }

    public static void clear(Player p) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team rt = sb.getTeam(TEAM_RED);
        org.bukkit.scoreboard.Team bt = sb.getTeam(TEAM_BLUE);
        if (rt != null) rt.removePlayer(p);
        if (bt != null) bt.removePlayer(p);
    }

    public static void clearAll() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team rt = sb.getTeam(TEAM_RED);
        org.bukkit.scoreboard.Team bt = sb.getTeam(TEAM_BLUE);
        if (rt != null) for (String n : new java.util.ArrayList<>(rt.getEntries())) rt.removeEntry(n);
        if (bt != null) for (String n : new java.util.ArrayList<>(bt.getEntries())) bt.removeEntry(n);
    }
}
