package my.pikrew.visantaraDungeonV2.commands;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonChancesCommand implements CommandExecutor, TabCompleter {

    private final VisantaraDungeonV2 plugin;

    public DungeonChancesCommand(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "dungeonreset":
                return handleReset(sender, args);
            case "dungeonreload":
                return handleReload(sender);
            default:
                return false;
        }
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dungeonchances.reset")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /dungeonreset <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        plugin.getDungeonChancesManager().resetPlayerChances(target.getUniqueId());
        plugin.getDungeonChancesManager().updateScoreboard(target);

        sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s dungeon lives!");
        target.sendMessage(ChatColor.GREEN + "Your dungeon lives have been reset by an admin!");

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("dungeonchances.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        plugin.reloadConfig();
        plugin.getDungeonChancesManager().reloadConfig();

        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("dungeonreset")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}