package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.Energy.Energy;
import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import my.pikrew.visantaraDungeonV2.models.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
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
        Entity clicked = event.getRightClicked();

        // Check for both ArmorStand and Slime (clickable entity)
        Hologram hologram = null;

        if (clicked instanceof ArmorStand) {
            hologram = plugin.getHologramManager().getHologramByEntity((ArmorStand) clicked);
        } else if (clicked instanceof Slime) {
            // Check if this is a hologram's clickable entity
            for (String holoId : plugin.getHologramManager().getHologramIds()) {
                Hologram holo = plugin.getHologramManager().getHologram(holoId);
                if (holo != null && holo.isClickableEntity(clicked)) {
                    hologram = holo;
                    break;
                }
            }
        }

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

        // Check energy requirement
        int energyCost = plugin.getEnergyManager().getDungeonEntryCost();
        Energy playerEnergy = plugin.getEnergyManager().getPlayerEnergy(player.getUniqueId());

        if (!plugin.getEnergyManager().hasEnoughEnergy(player.getUniqueId(), energyCost)) {
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "✗ Not enough energy to enter dungeon!");
            player.sendMessage(ChatColor.GRAY + "Required: " + ChatColor.GOLD + energyCost + " Energy");
            player.sendMessage(ChatColor.GRAY + "Current: " + ChatColor.GOLD + playerEnergy.getCurrentEnergy() +
                    ChatColor.DARK_GRAY + "/" + playerEnergy.getMaxEnergy());
            player.sendMessage(ChatColor.YELLOW + "Next energy in: " + ChatColor.WHITE +
                    playerEnergy.getTimeUntilNextEnergyFormatted());
            player.sendMessage("");
            return;
        }

        // Consume energy and enter dungeon
        if (plugin.getEnergyManager().consumeForDungeonEntry(player.getUniqueId())) {
            if (plugin.getDungeonManager().enterDungeon(player, dungeonName)) {
                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "═══════════════════════════════");
                player.sendMessage(ChatColor.GOLD + "⚔ " + ChatColor.YELLOW + "Welcome to " + dungeonName + "!");
                player.sendMessage(ChatColor.GRAY + "Energy consumed: " + ChatColor.GOLD + energyCost);
                player.sendMessage(ChatColor.GRAY + "Remaining energy: " + ChatColor.GOLD +
                        playerEnergy.getCurrentEnergy() + ChatColor.DARK_GRAY + "/" + playerEnergy.getMaxEnergy());
                player.sendMessage(ChatColor.GREEN + "═══════════════════════════════");
                player.sendMessage("");
                plugin.getLogger().info("Player " + player.getName() + " entered dungeon: " + dungeonName);
            } else {
                player.sendMessage(ChatColor.RED + "Failed to enter the dungeon!");
                // Refund energy if entry failed
                plugin.getEnergyManager().addEnergy(player.getUniqueId(), energyCost);
                plugin.getLogger().warning("Failed to teleport " + player.getName() + " to dungeon: " + dungeonName);
            }
        }
    }

    // Backup event handler for compatibility
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();

        // Check for both ArmorStand and Slime
        Hologram hologram = null;

        if (clicked instanceof ArmorStand) {
            hologram = plugin.getHologramManager().getHologramByEntity((ArmorStand) clicked);
        } else if (clicked instanceof Slime) {
            for (String holoId : plugin.getHologramManager().getHologramIds()) {
                Hologram holo = plugin.getHologramManager().getHologram(holoId);
                if (holo != null && holo.isClickableEntity(clicked)) {
                    hologram = holo;
                    break;
                }
            }
        }

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

        // Check energy requirement
        int energyCost = plugin.getEnergyManager().getDungeonEntryCost();
        Energy playerEnergy = plugin.getEnergyManager().getPlayerEnergy(player.getUniqueId());

        if (!plugin.getEnergyManager().hasEnoughEnergy(player.getUniqueId(), energyCost)) {
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "✗ Not enough energy to enter dungeon!");
            player.sendMessage(ChatColor.GRAY + "Required: " + ChatColor.GOLD + energyCost + " Energy");
            player.sendMessage(ChatColor.GRAY + "Current: " + ChatColor.GOLD + playerEnergy.getCurrentEnergy() +
                    ChatColor.DARK_GRAY + "/" + playerEnergy.getMaxEnergy());
            player.sendMessage(ChatColor.YELLOW + "Next energy in: " + ChatColor.WHITE +
                    playerEnergy.getTimeUntilNextEnergyFormatted());
            player.sendMessage("");
            return;
        }

        // Consume energy and enter dungeon
        if (plugin.getEnergyManager().consumeForDungeonEntry(player.getUniqueId())) {
            if (plugin.getDungeonManager().enterDungeon(player, dungeonName)) {
                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "═══════════════════════════════");
                player.sendMessage(ChatColor.GOLD + "⚔ " + ChatColor.YELLOW + "Welcome to " + dungeonName + "!");
                player.sendMessage(ChatColor.GRAY + "Energy consumed: " + ChatColor.GOLD + energyCost);
                player.sendMessage(ChatColor.GRAY + "Remaining energy: " + ChatColor.GOLD +
                        playerEnergy.getCurrentEnergy() + ChatColor.DARK_GRAY + "/" + playerEnergy.getMaxEnergy());
                player.sendMessage(ChatColor.GREEN + "═══════════════════════════════");
                player.sendMessage("");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to enter the dungeon!");
                // Refund energy if entry failed
                plugin.getEnergyManager().addEnergy(player.getUniqueId(), energyCost);
            }
        }
    }
}