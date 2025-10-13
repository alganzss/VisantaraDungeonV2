package my.pikrew.visantaraDungeonV2.managers;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final VisantaraDungeonV2 plugin;
    private final Map<UUID, Location> returnLocations;

    public PlayerManager(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        this.returnLocations = new HashMap<>();
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

    public void updateVisibility(Player player) {
        boolean playerInDungeon = plugin.getDungeonManager().isInDungeon(player);
        String playerDungeon = plugin.getDungeonManager().getPlayerDungeon(player.getUniqueId());

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (player.equals(other)) {
                continue;
            }

            boolean otherInDungeon = plugin.getDungeonManager().isInDungeon(other);
            String otherDungeon = plugin.getDungeonManager().getPlayerDungeon(other.getUniqueId());

            // If both in same dungeon or both not in dungeon, they can see each other
            if (playerInDungeon && otherInDungeon) {
                // Both in dungeon - hide from each other regardless of dungeon
                player.hidePlayer(plugin, other);
                other.hidePlayer(plugin, player);
            } else if (!playerInDungeon && !otherInDungeon) {
                // Both not in dungeon - show each other
                player.showPlayer(plugin, other);
                other.showPlayer(plugin, player);
            } else {
                // One in dungeon, one not - hide from each other
                player.hidePlayer(plugin, other);
                other.hidePlayer(plugin, player);
            }
        }
    }

    public void updateAllVisibility() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateVisibility(player);
        }
    }
}