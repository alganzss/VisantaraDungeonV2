package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import my.pikrew.visantaraDungeonV2.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class HologramListener implements Listener {

    private final VisantaraDungeonV2 plugin;

    public HologramListener(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        Hologram hologram = plugin.getHologramManager().getHologramByEntity(armorStand);

        if (hologram == null) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        String dungeonName = hologram.getDungeonName();

        if (plugin.getDungeonManager().getDungeon(dungeonName) == null) {
            player.sendMessage(ChatColor.RED + "This dungeon no longer exists!");
            return;
        }

        if (plugin.getDungeonManager().isInDungeon(player)) {
            player.sendMessage(ChatColor.RED + "You are already in a dungeon! Use /vdungeon exit first.");
            return;
        }

        if (plugin.getDungeonManager().enterDungeon(player, dungeonName)) {
            player.sendMessage(ChatColor.GREEN + "Welcome to " + dungeonName + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to enter the dungeon!");
        }
    }
}
