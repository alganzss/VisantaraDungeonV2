package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerVisibilityListener implements Listener {

    private final VisantaraDungeonV2 plugin;

    public PlayerVisibilityListener(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Delay to ensure player is fully loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().updateAllVisibility();
        }, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getDungeonManager().isInDungeon(event.getPlayer())) {
            plugin.getDungeonManager().exitDungeon(event.getPlayer());
        }

        // Update visibility for remaining players
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().updateAllVisibility();
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // Delay to ensure world change is complete
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().updateAllVisibility();
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Update visibility after teleport
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().updateAllVisibility();
        }, 5L);
    }
}