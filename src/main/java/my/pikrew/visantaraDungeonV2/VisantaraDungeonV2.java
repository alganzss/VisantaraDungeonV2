package my.pikrew.visantaraDungeonV2;

import my.pikrew.visantaraDungeonV2.commands.VDungeonCommand;
import my.pikrew.visantaraDungeonV2.commands.EditMenuCommand;
import my.pikrew.visantaraDungeonV2.commands.PartyCommand;
import my.pikrew.visantaraDungeonV2.commands.DungeonChancesCommand;
import my.pikrew.visantaraDungeonV2.listeners.HologramListener;
import my.pikrew.visantaraDungeonV2.listeners.PlayerVisibilityListener;
import my.pikrew.visantaraDungeonV2.listeners.EditMenuListener;
import my.pikrew.visantaraDungeonV2.listeners.DungeonChancesListener;
import my.pikrew.visantaraDungeonV2.managers.DungeonManager;
import my.pikrew.visantaraDungeonV2.managers.HologramManager;
import my.pikrew.visantaraDungeonV2.managers.PlayerManager;
import my.pikrew.visantaraDungeonV2.managers.PartyManager;
import my.pikrew.visantaraDungeonV2.managers.DungeonChancesManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for VisantaraDungeonV2
 * Advanced dungeon system with hologram entrance, party system, and player visibility management
 *
 * @author Pikrew
 * @version 1.0
 */
public class VisantaraDungeonV2 extends JavaPlugin {

    private static VisantaraDungeonV2 instance;

    // Managers
    private DungeonManager dungeonManager;
    private HologramManager hologramManager;
    private PlayerManager playerManager;
    private PartyManager partyManager;
    private DungeonChancesManager dungeonChancesManager;

    @Override
    public void onEnable() {
        instance = this;

        // Create plugin folder if not exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Load configuration
        saveDefaultConfig();

        // Initialize managers in correct order
        // Order is important: Party must be initialized before Player
        getLogger().info("Initializing managers...");

        dungeonManager = new DungeonManager(this);
        hologramManager = new HologramManager(this);
        partyManager = new PartyManager(this);
        playerManager = new PlayerManager(this);
        dungeonChancesManager = new DungeonChancesManager(this);

        // Register commands
        getLogger().info("Registering commands...");
        registerCommands();

        // Register event listeners
        getLogger().info("Registering event listeners...");
        registerListeners();

        // Update player visibility after server fully loads
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (playerManager != null) {
                playerManager.updateAllVisibility();
                getLogger().info("Player visibility updated!");
            }
        }, 20L); // Wait 1 second (20 ticks)

        getLogger().info("======================================");
        getLogger().info("VDungeon plugin has been enabled!");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: " + getDescription().getAuthors());
        getLogger().info("======================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving plugin data...");

        // Save dungeon data
        if (dungeonManager != null) {
            dungeonManager.saveAll();
            getLogger().info("Dungeon data saved!");
        }

        // Remove all holograms
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
            getLogger().info("Holograms removed!");
        }

        // Save player settings
        if (playerManager != null) {
            playerManager.savePlayerSettings();
            getLogger().info("Player settings saved!");
        }

        // Remove all scoreboards
        if (dungeonChancesManager != null) {
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                dungeonChancesManager.removeScoreboard(player);
            }
            getLogger().info("Scoreboards removed!");
        }

        getLogger().info("======================================");
        getLogger().info("VDungeon plugin has been disabled!");
        getLogger().info("======================================");
    }

    /**
     * Register all commands with their executors and tab completers
     */
    private void registerCommands() {
        // VDungeon command
        VDungeonCommand vdCommand = new VDungeonCommand(this);
        getCommand("vdungeon").setExecutor(vdCommand);
        getCommand("vdungeon").setTabCompleter(vdCommand);

        // EditMenu command
        getCommand("editmenu").setExecutor(new EditMenuCommand(this));

        // Party command
        PartyCommand partyCommand = new PartyCommand(this);
        getCommand("party").setExecutor(partyCommand);
        getCommand("party").setTabCompleter(partyCommand);

        // DungeonChances commands
        DungeonChancesCommand chancesCommand = new DungeonChancesCommand(this);
        getCommand("dungeonreset").setExecutor(chancesCommand);
        getCommand("dungeonreset").setTabCompleter(chancesCommand);
        getCommand("dungeonreload").setExecutor(chancesCommand);
    }

    /**
     * Register all event listeners
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new HologramListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVisibilityListener(this), this);
        getServer().getPluginManager().registerEvents(new EditMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new DungeonChancesListener(this), this);
    }

    /**
     * Get plugin instance
     * @return Plugin instance
     */
    public static VisantaraDungeonV2 getInstance() {
        return instance;
    }

    /**
     * Get dungeon manager
     * @return DungeonManager instance
     */
    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    /**
     * Get hologram manager
     * @return HologramManager instance
     */
    public HologramManager getHologramManager() {
        return hologramManager;
    }

    /**
     * Get player manager
     * @return PlayerManager instance
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Get party manager
     * @return PartyManager instance
     */
    public PartyManager getPartyManager() {
        return partyManager;
    }

    /**
     * Get dungeon chances manager
     * @return DungeonChancesManager instance
     */
    public DungeonChancesManager getDungeonChancesManager() {
        return dungeonChancesManager;
    }
}