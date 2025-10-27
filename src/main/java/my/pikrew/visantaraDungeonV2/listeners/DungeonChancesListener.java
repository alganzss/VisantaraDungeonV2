package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DungeonChancesListener implements Listener {

    private final VisantaraDungeonV2 plugin;

    public DungeonChancesListener(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDungeonChancesManager().initializePlayer(player);

        // Delay untuk memastikan player fully loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getDungeonManager().isInDungeon(player)) {
                plugin.getDungeonChancesManager().updateScoreboard(player);
            }
        }, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Hanya proses jika player di dungeon
        if (plugin.getDungeonManager().isInDungeon(player)) {
            plugin.getDungeonChancesManager().handlePlayerDeath(player);

            // Log untuk debugging
            plugin.getLogger().info("Player " + player.getName() + " died in dungeon.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Hanya proses jika player di dungeon
        if (plugin.getDungeonManager().isInDungeon(player)) {
            String dungeonName = plugin.getDungeonManager().getPlayerDungeon(player.getUniqueId());

            if (dungeonName != null) {
                Location dungeonSpawn = plugin.getDungeonManager()
                        .getDungeon(dungeonName)
                        .getSpawnLocation();

                if (dungeonSpawn != null) {
                    // Set respawn location ke dungeon spawn
                    event.setRespawnLocation(dungeonSpawn);
                    plugin.getLogger().info("Set respawn location for " + player.getName() + " to dungeon spawn.");
                }
            }

            // Handle respawn logic (kick jika lives habis) dengan delay
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getDungeonChancesManager().handlePlayerRespawn(player);
            }, 5L);
        }
    }
}