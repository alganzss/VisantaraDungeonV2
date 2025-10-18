package my.pikrew.visantaraDungeonV2;

import my.pikrew.visantaraDungeonV2.commands.VDungeonCommand;
import my.pikrew.visantaraDungeonV2.commands.EditMenuCommand;
import my.pikrew.visantaraDungeonV2.listeners.HologramListener;
import my.pikrew.visantaraDungeonV2.listeners.PlayerVisibilityListener;
import my.pikrew.visantaraDungeonV2.listeners.EditMenuListener;
import my.pikrew.visantaraDungeonV2.managers.DungeonManager;
import my.pikrew.visantaraDungeonV2.managers.HologramManager;
import my.pikrew.visantaraDungeonV2.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VisantaraDungeonV2 extends JavaPlugin {

    private static VisantaraDungeonV2 instance;
    private DungeonManager dungeonManager;
    private HologramManager hologramManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        instance = this;

        // Create plugin folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Load config
        saveDefaultConfig();

        // Initialize managers
        dungeonManager = new DungeonManager(this);
        hologramManager = new HologramManager(this);
        playerManager = new PlayerManager(this);

        // Register commands
        getCommand("vdungeon").setExecutor(new VDungeonCommand(this));
        getCommand("editmenu").setExecutor(new EditMenuCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new HologramListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVisibilityListener(this), this);
        getServer().getPluginManager().registerEvents(new EditMenuListener(this), this);

        // Update all player visibility on startup (delay to ensure all players are loaded)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            playerManager.updateAllVisibility();
        }, 20L);

        getLogger().info("VDungeon plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all data
        if (dungeonManager != null) {
            dungeonManager.saveAll();
        }

        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }

        if (playerManager != null) {
            playerManager.savePlayerSettings();
        }

        getLogger().info("VDungeon plugin has been disabled!");
    }

    public static VisantaraDungeonV2 getInstance() {
        return instance;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}