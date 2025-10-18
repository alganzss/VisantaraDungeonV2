package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import my.pikrew.visantaraDungeonV2.models.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class HologramListener implements Listener {

    private final VisantaraDungeonV2 plugin;

    public HologramListener(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
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

        plugin.getLogger().info("Player " + player.getName() + " clicked hologram for dungeon: " + dungeonName);

        if (plugin.getDungeonManager().getDungeon(dungeonName) == null) {
            player.sendMessage(ChatColor.RED + "This dungeon no longer exists!");
            plugin.getLogger().warning("Dungeon " + dungeonName + " does not exist!");
            return;
        }

        if (plugin.getDungeonManager().isInDungeon(player)) {
            player.sendMessage(ChatColor.RED + "You are already in a dungeon! Use /vdungeon exit first.");
            return;
        }

        if (plugin.getDungeonManager().enterDungeon(player, dungeonName)) {
            player.sendMessage(ChatColor.GREEN + "Welcome to " + dungeonName + "!");
            plugin.getLogger().info("Player " + player.getName() + " entered dungeon: " + dungeonName);
        } else {
            player.sendMessage(ChatColor.RED + "Failed to enter the dungeon!");
            plugin.getLogger().warning("Failed to teleport " + player.getName() + " to dungeon: " + dungeonName);
        }
    }

    // Backup event handler untuk kompatibilitas
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
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

        plugin.getLogger().info("Player " + player.getName() + " interacted with hologram for dungeon: " + dungeonName);

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