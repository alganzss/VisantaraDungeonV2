package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.Bukkit;
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
        plugin.getDungeonChancesManager().handlePlayerDeath(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getDungeonChancesManager().handlePlayerRespawn(player);
    }
}
