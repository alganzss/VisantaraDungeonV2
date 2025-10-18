package my.pikrew.visantaraDungeonV2.commands;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class EditMenuCommand implements CommandExecutor {

    private final VisantaraDungeonV2 plugin;

    public EditMenuCommand(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        openEditMenu(player);
        return true;
    }

    public void openEditMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Edit Menu - Player Visibility");

        boolean canSeeOthers = plugin.getPlayerManager().canSeeOtherPlayers(player.getUniqueId());

        // Item untuk Hide Players
        ItemStack hideItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta hideMeta = hideItem.getItemMeta();
        hideMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Hide All Players");
        hideMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to hide all other players",
                ChatColor.GRAY + "from your view",
                "",
                canSeeOthers ? ChatColor.YELLOW + "Status: " + ChatColor.GREEN + "Players Visible" : ChatColor.YELLOW + "Status: " + ChatColor.RED + "Players Hidden",
                "",
                ChatColor.DARK_GRAY + "You will not see any other players"
        ));
        hideItem.setItemMeta(hideMeta);

        // Item untuk Show Players
        ItemStack showItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta showMeta = showItem.getItemMeta();
        showMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Show All Players");
        showMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to show all other players",
                ChatColor.GRAY + "in your view",
                "",
                canSeeOthers ? ChatColor.YELLOW + "Status: " + ChatColor.GREEN + "Players Visible" : ChatColor.YELLOW + "Status: " + ChatColor.RED + "Players Hidden",
                "",
                ChatColor.DARK_GRAY + "You will see all players normally"
        ));
        showItem.setItemMeta(showMeta);

        // Item Status
        ItemStack statusItem = new ItemStack(canSeeOthers ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta statusMeta = statusItem.getItemMeta();
        statusMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Current Status");
        statusMeta.setLore(Arrays.asList(
                "",
                canSeeOthers ? ChatColor.GREEN + "✔ Players are visible" : ChatColor.RED + "✘ Players are hidden",
                "",
                ChatColor.GRAY + "Choose an option above to change"
        ));
        statusItem.setItemMeta(statusMeta);

        // Item Close
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Close Menu");
        closeMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to close this menu"
        ));
        closeItem.setItemMeta(closeMeta);

        // Fill dengan glass pane
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            menu.setItem(i, filler);
        }

        // Set items
        menu.setItem(11, hideItem);
        menu.setItem(13, statusItem);
        menu.setItem(15, showItem);
        menu.setItem(22, closeItem);

        player.openInventory(menu);
    }
}