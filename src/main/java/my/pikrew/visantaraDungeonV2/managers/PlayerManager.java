package my.pikrew.visantaraDungeonV2.managers;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final VisantaraDungeonV2 plugin;
    private final Map<UUID, Location> returnLocations;
    private final Map<UUID, Boolean> playerVisibilitySettings;
    private File settingsFile;
    private FileConfiguration settingsConfig;

    public PlayerManager(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        this.returnLocations = new HashMap<>();
        this.playerVisibilitySettings = new HashMap<>();
        loadPlayerSettings();
    }

    public void setReturnLocation(UUID playerId, Location location) {
        returnLocations.put(playerId, location);
    }

    public Location getReturnLocation(UUID playerId) {
        return returnLocations.get(playerId);
    }

    public void removeReturnLocation(UUID playerId) {
        returnLocations.remove(playerId);
    }

    public boolean canSeeOtherPlayers(UUID playerId) {
        return playerVisibilitySettings.getOrDefault(playerId, true);
    }

    public void setCanSeeOtherPlayers(UUID playerId, boolean canSee) {
        playerVisibilitySettings.put(playerId, canSee);
        savePlayerSettings();
    }

    public void updateVisibility(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        boolean playerInDungeon = plugin.getDungeonManager().isInDungeon(player);
        String playerDungeon = plugin.getDungeonManager().getPlayerDungeon(player.getUniqueId());
        boolean playerCanSeeOthers = canSeeOtherPlayers(player.getUniqueId());

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (player.equals(other) || !other.isOnline()) {
                continue;
            }

            boolean otherInDungeon = plugin.getDungeonManager().isInDungeon(other);
            String otherDungeon = plugin.getDungeonManager().getPlayerDungeon(other.getUniqueId());
            boolean otherCanSeeOthers = canSeeOtherPlayers(other.getUniqueId());

            // If player chose to hide - hide all others from player's view AND hide player from others
            if (!playerCanSeeOthers) {
                player.hidePlayer(plugin, other);
                other.hidePlayer(plugin, player); // Player becomes invisible to others too
            }
            // If other player chose to hide - hide other from player's view
            else if (!otherCanSeeOthers) {
                player.hidePlayer(plugin, other);
                other.hidePlayer(plugin, player); // Other is invisible to everyone
            }
            // Both players have visibility enabled - check dungeon logic
            else {
                // Both players in same dungeon - they can see each other
                if (playerInDungeon && otherInDungeon && playerDungeon != null && playerDungeon.equals(otherDungeon)) {
                    player.showPlayer(plugin, other);
                    other.showPlayer(plugin, player);
                }
                // Both players NOT in any dungeon - they can see each other
                else if (!playerInDungeon && !otherInDungeon) {
                    player.showPlayer(plugin, other);
                    other.showPlayer(plugin, player);
                }
                // All other cases: different dungeons or one in dungeon and one not - hide from each other
                else {
                    player.hidePlayer(plugin, other);
                    other.hidePlayer(plugin, player);
                }
            }
        }
    }

    public void updateAllVisibility() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline()) {
                updateVisibility(player);
            }
        }
    }

    private void loadPlayerSettings() {
        settingsFile = new File(plugin.getDataFolder(), "player_settings.yml");
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        if (settingsConfig.contains("players")) {
            for (String uuidStr : settingsConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                boolean canSee = settingsConfig.getBoolean("players." + uuidStr + ".can-see-others", true);
                playerVisibilitySettings.put(uuid, canSee);
            }
        }
    }

    public void savePlayerSettings() {
        for (Map.Entry<UUID, Boolean> entry : playerVisibilitySettings.entrySet()) {
            String uuidStr = entry.getKey().toString();
            settingsConfig.set("players." + uuidStr + ".can-see-others", entry.getValue());
        }

        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}