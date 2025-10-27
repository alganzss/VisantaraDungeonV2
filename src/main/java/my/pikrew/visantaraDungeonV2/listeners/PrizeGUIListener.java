package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PrizeGUIListener implements Listener {

    private final VisantaraDungeonV2 plugin;

    public PrizeGUIListener(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.contains("Prize Selection")) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        // Prize slots (11, 13, 15)
        if (slot == 11 || slot == 13 || slot == 15) {
            handlePrizeClaim(player, slot);
        }
        // Reroll button (slot 22)
        else if (slot == 22 && clickedItem.getType() == Material.ARROW) {
            handleReroll(player);
        }
        // Close button (slot 18)
        else if (slot == 18 && clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
        }
    }

    private void handlePrizeClaim(Player player, int slot) {
        int claimCost = plugin.getEnergyManager().getPrizeClaimCost();

        // Check if player has enough energy
        if (!plugin.getEnergyManager().hasEnoughEnergy(player.getUniqueId(), claimCost)) {
            player.sendMessage(ChatColor.RED + "✗ Not enough energy!");
            player.sendMessage(ChatColor.GRAY + "You need " + ChatColor.GOLD + claimCost +
                    ChatColor.GRAY + " energy to claim a prize.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Consume energy
        if (!plugin.getEnergyManager().consumeForPrizeClaim(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Failed to claim prize!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Claim prize
        if (plugin.getPrizeManager().claimPrize(player, slot)) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "═══════════════════════════════");
            player.sendMessage(ChatColor.GOLD + "⭐ " + ChatColor.YELLOW + "Prize Claimed!");
            player.sendMessage(ChatColor.GRAY + "Check your inventory for your reward!");
            player.sendMessage(ChatColor.DARK_GRAY + "Energy consumed: " + claimCost);
            player.sendMessage(ChatColor.GREEN + "═══════════════════════════════");
            player.sendMessage("");

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            player.closeInventory();
        } else {
            player.sendMessage(ChatColor.RED + "Failed to claim prize!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    private void handleReroll(Player player) {
        int rerollCost = plugin.getEnergyManager().getPrizeRerollCost();

        // Check if player has enough energy
        if (!plugin.getEnergyManager().hasEnoughEnergy(player.getUniqueId(), rerollCost)) {
            player.sendMessage(ChatColor.RED + "✗ Not enough energy to reroll!");
            player.sendMessage(ChatColor.GRAY + "You need " + ChatColor.GOLD + rerollCost +
                    ChatColor.GRAY + " energy to reroll prizes.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Consume energy
        if (!plugin.getEnergyManager().consumeForPrizeReroll(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Failed to reroll prizes!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Reroll prizes
        plugin.getPrizeManager().rerollPrizes(player.getUniqueId());

        player.sendMessage(ChatColor.YELLOW + "⟳ Prizes rerolled! Energy consumed: " + rerollCost);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);

        // Reopen GUI with new prizes
        player.closeInventory();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getPrizeManager().openPrizeGUI(player);
        }, 2L);
    }
}