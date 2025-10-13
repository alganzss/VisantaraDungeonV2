package my.pikrew.visantaraDungeonV2;

import my.pikrew.visantaraDungeonV2.VDungeonCommand;
import my.pikrew.visantaraDungeonV2.HologramListener;
import my.pikrew.visantaraDungeonV2.PlayerVisibilityListener;
import my.pikrew.visantaraDungeonV2.DungeonManager;
import my.pikrew.visantaraDungeonV2.HologramManager;
import my.pikrew.visantaraDungeonV2.PlayerManager;
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

        // Register listeners
        getServer().getPluginManager().registerEvents(new HologramListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVisibilityListener(this), this);

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

        getLogger().info("VDungeon plugin has been disabled!");
    }

    public static VDungeon getInstance() {
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