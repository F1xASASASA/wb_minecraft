package net.cubo.woolbrawl;

import net.cubo.woolbrawl.command.WbCommand;
import net.cubo.woolbrawl.config.WbConfig;
import net.cubo.woolbrawl.game.GameManager;
import net.cubo.woolbrawl.kit.KitRegistry;
import net.cubo.woolbrawl.listener.*;
import org.bukkit.plugin.java.JavaPlugin;

public class WoolBrawlPlugin extends JavaPlugin {

    private static WoolBrawlPlugin instance;
    private WbConfig wbConfig;
    private KitRegistry kitRegistry;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.wbConfig = new WbConfig(this);
        this.kitRegistry = new KitRegistry();
        this.gameManager = new GameManager(this);

        net.cubo.woolbrawl.game.ScoreboardHelper.ensureTeams();

        getCommand("wb").setExecutor(new WbCommand(this));

        getServer().getPluginManager().registerEvents(new KitSelectListener(this), this);
        getServer().getPluginManager().registerEvents(new BreakListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new TntListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new MiscListener(this), this);

        getLogger().info("WoolBrawl загружен. /wb help для команд.");
    }

    @Override
    public void onDisable() {
        if (gameManager != null && gameManager.isRunning()) {
            gameManager.stop();
        }
    }

    public static WoolBrawlPlugin get() { return instance; }
    public WbConfig cfg() { return wbConfig; }
    public KitRegistry kits() { return kitRegistry; }
    public GameManager game() { return gameManager; }
}
