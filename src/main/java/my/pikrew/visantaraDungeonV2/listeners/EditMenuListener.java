package my.pikrew.visantaraDungeonV2.listeners;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import my.pikrew.visantaraDungeonV2.commands.EditMenuCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EditMenuListener implements Listener {

    private final VisantaraDungeonV2 plugin;

    public EditMenuListener(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.DARK_PURPLE + "Edit Menu - Player Visibility")) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Hide Players
        if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
            plugin.getPlayerManager().setCanSeeOtherPlayers(player.getUniqueId(), false);
            plugin.getPlayerManager().updateVisibility(player);
            player.sendMessage(ChatColor.RED + "You have hidden all other players!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            player.closeInventory();
        }
        // Show Players
        else if (clickedItem.getType() == Material.LIME_STAINED_GLASS_PANE) {
            plugin.getPlayerManager().setCanSeeOtherPlayers(player.getUniqueId(), true);
            plugin.getPlayerManager().updateVisibility(player);
            player.sendMessage(ChatColor.GREEN + "You can now see all other players!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        }
        // Status Item - Refresh menu
        else if (clickedItem.getType() == Material.LIME_DYE || clickedItem.getType() == Material.RED_DYE) {
            EditMenuCommand cmd = new EditMenuCommand(plugin);
            cmd.openEditMenu(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
        // Close
        else if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
        }
    }
}