package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EnergyListener implements Listener {

    private final VisantaraDungeonV2 plugin;

    public EnergyListener(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize player energy (will update based on offline time)
        plugin.getEnergyManager().initializePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save player energy data when they quit
        plugin.getEnergyManager().saveEnergyData();
    }
}