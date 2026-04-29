package net.cubo.woolbrawl.game;

import net.cubo.woolbrawl.kit.Kit;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private Team team;
    private Kit kit;
    private boolean alive = true;

    public PlayerData(UUID uuid) { this.uuid = uuid; }

    public UUID uuid() { return uuid; }
    public Team team() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public Kit kit() { return kit; }
    public void setKit(Kit kit) { this.kit = kit; }
    public boolean alive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
}
