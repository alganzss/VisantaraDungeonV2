package my.pikrew.visantaraDungeonV2.Energy;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnergyManager {

    private final VisantaraDungeonV2 plugin;
    private final Map<UUID, Energy> playerEnergy;
    private File energyFile;
    private FileConfiguration energyConfig;

    // Config values
    private int dungeonEntryCost;
    private int prizeClaimCost;
    private int prizeRerollCost;

    public EnergyManager(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        this.playerEnergy = new HashMap<>();
        loadConfig();
        loadEnergyData();
        startEnergyUpdateTask();
    }

    private void loadConfig() {
        dungeonEntryCost = plugin.getConfig().getInt("energy.dungeon-entry-cost", 25);
        prizeClaimCost = plugin.getConfig().getInt("energy.prize-claim-cost", 30);
        prizeRerollCost = plugin.getConfig().getInt("energy.prize-reroll-cost", 5);
    }

    public void reloadConfig() {
        loadConfig();
    }

    /**
     * Initialize player energy
     */
    public void initializePlayer(UUID playerId) {
        if (!playerEnergy.containsKey(playerId)) {
            playerEnergy.put(playerId, new Energy(playerId));
        } else {
            // Update energy based on time passed
            playerEnergy.get(playerId).updateEnergy();
        }
    }

    /**
     * Get player energy object
     */
    public Energy getPlayerEnergy(UUID playerId) {
        initializePlayer(playerId);
        return playerEnergy.get(playerId);
    }

    /**
     * Check if player has enough energy
     */
    public boolean hasEnoughEnergy(UUID playerId, int amount) {
        Energy energy = getPlayerEnergy(playerId);
        return energy.getCurrentEnergy() >= amount;
    }

    /**
     * Consume energy for dungeon entry
     */
    public boolean consumeForDungeonEntry(UUID playerId) {
        Energy energy = getPlayerEnergy(playerId);
        return energy.consumeEnergy(dungeonEntryCost);
    }

    /**
     * Consume energy for prize claim
     */
    public boolean consumeForPrizeClaim(UUID playerId) {
        Energy energy = getPlayerEnergy(playerId);
        return energy.consumeEnergy(prizeClaimCost);
    }

    /**
     * Consume energy for prize reroll
     */
    public boolean consumeForPrizeReroll(UUID playerId) {
        Energy energy = getPlayerEnergy(playerId);
        return energy.consumeEnergy(prizeRerollCost);
    }

    /**
     * Add energy to player (admin command)
     */
    public void addEnergy(UUID playerId, int amount) {
        Energy energy = getPlayerEnergy(playerId);
        energy.addEnergy(amount);
    }

    /**
     * Set player energy (admin command)
     */
    public void setEnergy(UUID playerId, int amount) {
        Energy energy = getPlayerEnergy(playerId);
        energy.setCurrentEnergy(amount);
    }

    /**
     * Get dungeon entry cost
     */
    public int getDungeonEntryCost() {
        return dungeonEntryCost;
    }

    /**
     * Get prize claim cost
     */
    public int getPrizeClaimCost() {
        return prizeClaimCost;
    }

    /**
     * Get prize reroll cost
     */
    public int getPrizeRerollCost() {
        return prizeRerollCost;
    }

    /**
     * Start periodic energy update task
     */
    private void startEnergyUpdateTask() {
        // Update energy every minute for online players
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                Energy energy = playerEnergy.get(player.getUniqueId());
                if (energy != null) {
                    energy.updateEnergy();
                }
            }
        }, 1200L, 1200L); // Run every minute (1200 ticks)
    }

    /**
     * Load energy data from file
     */
    private void loadEnergyData() {
        energyFile = new File(plugin.getDataFolder(), "energy.yml");
        if (!energyFile.exists()) {
            try {
                energyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        energyConfig = YamlConfiguration.loadConfiguration(energyFile);

        if (energyConfig.contains("energy")) {
            for (String uuidStr : energyConfig.getConfigurationSection("energy").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    int currentEnergy = energyConfig.getInt("energy." + uuidStr + ".current");
                    long lastUpdate = energyConfig.getLong("energy." + uuidStr + ".last-update");

                    Energy energy = new Energy(playerId, currentEnergy, lastUpdate);
                    energy.updateEnergy(); // Update based on offline time
                    playerEnergy.put(playerId, energy);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in energy.yml: " + uuidStr);
                }
            }
        }

        plugin.getLogger().info("Loaded energy data for " + playerEnergy.size() + " players.");
    }

    /**
     * Save energy data to file
     */
    public void saveEnergyData() {
        for (Map.Entry<UUID, Energy> entry : playerEnergy.entrySet()) {
            String uuidStr = entry.getKey().toString();
            Energy energy = entry.getValue();

            energy.updateEnergy(); // Update before saving

            energyConfig.set("energy." + uuidStr + ".current", energy.getCurrentEnergy());
            energyConfig.set("energy." + uuidStr + ".last-update", energy.getLastUpdate());
        }

        try {
            energyConfig.save(energyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}