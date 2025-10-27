package my.pikrew.visantaraDungeonV2.commands;

import my.pikrew.visantaraDungeonV2.Energy.Energy;
import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VDungeonCommand implements CommandExecutor, TabCompleter {

    private final VisantaraDungeonV2 plugin;

    public VDungeonCommand(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "play":
                return handlePlay(sender, args);
            case "exit":
                return handleExit(sender, args);
            case "hologram":
                return handleHologram(sender, args);
            case "list":
                return handleList(sender, args);
            case "setspawn":
                return handleSetSpawn(sender, args);
            case "prize":
                return handlePrize(sender, args);
            case "energy":
                return handleEnergy(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vdungeon.create")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /vdungeon create <name>");
            return true;
        }

        String dungeonName = args[1];

        if (plugin.getDungeonManager().createDungeon(dungeonName)) {
            sender.sendMessage(ChatColor.GREEN + "Dungeon '" + dungeonName + "' has been created!");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to create dungeon! Dungeon might already exist.");
        }

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vdungeon.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /vdungeon delete <name>");
            return true;
        }

        String dungeonName = args[1];

        if (plugin.getDungeonManager().deleteDungeon(dungeonName)) {
            sender.sendMessage(ChatColor.GREEN + "Dungeon '" + dungeonName + "' has been deleted!");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete dungeon! Dungeon might not exist.");
        }

        return true;
    }

    private boolean handlePlay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /vdungeon play <name>");
            return true;
        }

        Player player = (Player) sender;
        String dungeonName = args[1];

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
            return true;
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
                player.sendMessage(ChatColor.RED + "Failed to enter dungeon! Dungeon might not exist.");
                // Refund energy if entry failed
                plugin.getEnergyManager().addEnergy(player.getUniqueId(), energyCost);
            }
        }

        return true;
    }

    private boolean handleExit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getDungeonManager().isInDungeon(player)) {
            player.sendMessage(ChatColor.RED + "You are not in a dungeon!");
            return true;
        }

        plugin.getDungeonManager().exitDungeon(player);
        player.sendMessage(ChatColor.GREEN + "You have exited the dungeon!");

        return true;
    }

    private boolean handlePrize(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getPrizeManager().openPrizeGUI(player);
        return true;
    }

    private boolean handleEnergy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        Energy energy = plugin.getEnergyManager().getPlayerEnergy(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "ENERGY STATUS");
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "⚡ Current Energy: " + ChatColor.WHITE +
                energy.getCurrentEnergy() + ChatColor.GRAY + "/" + energy.getMaxEnergy());

        if (energy.getCurrentEnergy() < energy.getMaxEnergy()) {
            player.sendMessage(ChatColor.YELLOW + "⏱ Next energy in: " + ChatColor.WHITE +
                    energy.getTimeUntilNextEnergyFormatted());
        } else {
            player.sendMessage(ChatColor.GREEN + "✓ Energy is full!");
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Energy Costs:");
        player.sendMessage(ChatColor.GOLD + "  • Dungeon Entry: " + ChatColor.WHITE +
                plugin.getEnergyManager().getDungeonEntryCost() + " Energy");
        player.sendMessage(ChatColor.GOLD + "  • Claim Prize: " + ChatColor.WHITE +
                plugin.getEnergyManager().getPrizeClaimCost() + " Energy");
        player.sendMessage(ChatColor.GOLD + "  • Reroll Prize: " + ChatColor.WHITE +
                plugin.getEnergyManager().getPrizeRerollCost() + " Energy");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "Energy regenerates 1 per 5 minutes");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage("");

        return true;
    }

    private boolean handleToggleEnergy(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vdungeon.toggleenergy")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        boolean currentState = plugin.getConfig().getBoolean("energy.display.enabled", true);
        boolean newState = !currentState;

        plugin.getConfig().set("energy.display.enabled", newState);
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "Energy action bar display: " +
                (newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));

        return true;
    }

    private boolean handleHologram(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /vdungeon hologram <create|delete|list>");
            return true;
        }

        String action = args[1].toLowerCase();
        Player player = (Player) sender;

        switch (action) {
            case "create":
                if (!sender.hasPermission("vdungeon.hologram.create")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /vdungeon hologram create <id> <dungeon> <line1> [line2] [line3]...");
                    return true;
                }

                String holoId = args[2];
                String dungeonName = args[3];

                if (plugin.getDungeonManager().getDungeon(dungeonName) == null) {
                    sender.sendMessage(ChatColor.RED + "Dungeon '" + dungeonName + "' does not exist!");
                    return true;
                }

                List<String> lines = new ArrayList<>();
                for (int i = 4; i < args.length; i++) {
                    lines.add(args[i].replace("_", " "));
                }

                if (lines.isEmpty()) {
                    lines.add("&e&lClick to enter");
                    lines.add("&7" + dungeonName);
                }

                if (plugin.getHologramManager().createHologram(holoId, player.getLocation(), lines, dungeonName)) {
                    sender.sendMessage(ChatColor.GREEN + "Hologram '" + holoId + "' has been created!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to create hologram! Hologram might already exist.");
                }
                break;

            case "delete":
                if (!sender.hasPermission("vdungeon.hologram.delete")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /vdungeon hologram delete <id>");
                    return true;
                }

                String deleteId = args[2];

                if (plugin.getHologramManager().deleteHologram(deleteId)) {
                    sender.sendMessage(ChatColor.GREEN + "Hologram '" + deleteId + "' has been deleted!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to delete hologram! Hologram might not exist.");
                }
                break;

            case "list":
                if (!sender.hasPermission("vdungeon.hologram.list")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }

                if (plugin.getHologramManager().getHologramIds().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No holograms found!");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Holograms:");
                    for (String id : plugin.getHologramManager().getHologramIds()) {
                        sender.sendMessage(ChatColor.GRAY + "- " + id);
                    }
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Usage: /vdungeon hologram <create|delete|list>");
                break;
        }

        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vdungeon.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (plugin.getDungeonManager().getDungeonNames().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No dungeons found!");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Available dungeons:");
            for (String name : plugin.getDungeonManager().getDungeonNames()) {
                sender.sendMessage(ChatColor.GRAY + "- " + name);
            }
        }

        return true;
    }

    private boolean handleSetSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (!sender.hasPermission("vdungeon.setspawn")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /vdungeon setspawn <dungeon>");
            return true;
        }

        Player player = (Player) sender;
        String dungeonName = args[1];

        if (plugin.getDungeonManager().getDungeon(dungeonName) == null) {
            sender.sendMessage(ChatColor.RED + "Dungeon '" + dungeonName + "' does not exist!");
            return true;
        }

        plugin.getDungeonManager().getDungeon(dungeonName).setSpawnLocation(player.getLocation());
        plugin.getDungeonManager().saveAll();
        sender.sendMessage(ChatColor.GREEN + "Spawn location for dungeon '" + dungeonName + "' has been set!");

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== VDungeon Commands ==========");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon create <name>" + ChatColor.GRAY + " - Create a new dungeon");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon delete <name>" + ChatColor.GRAY + " - Delete a dungeon");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon play <name>" + ChatColor.GRAY + " - Enter a dungeon");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon exit" + ChatColor.GRAY + " - Exit current dungeon");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon list" + ChatColor.GRAY + " - List all dungeons");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon setspawn <name>" + ChatColor.GRAY + " - Set dungeon spawn");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon prize" + ChatColor.GRAY + " - Open prize GUI");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon energy" + ChatColor.GRAY + " - Check your energy");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon hologram create <id> <dungeon> <lines...>" + ChatColor.GRAY + " - Create hologram");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon hologram delete <id>" + ChatColor.GRAY + " - Delete hologram");
        sender.sendMessage(ChatColor.YELLOW + "/vdungeon hologram list" + ChatColor.GRAY + " - List all holograms");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "delete", "play", "exit", "list", "hologram", "setspawn", "prize", "energy", "toggleenergy"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("play") || args[0].equalsIgnoreCase("setspawn")) {
                completions.addAll(plugin.getDungeonManager().getDungeonNames());
            } else if (args[0].equalsIgnoreCase("hologram")) {
                completions.addAll(Arrays.asList("create", "delete", "list"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("hologram") && args[1].equalsIgnoreCase("delete")) {
                completions.addAll(plugin.getHologramManager().getHologramIds());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("hologram") && args[1].equalsIgnoreCase("create")) {
                completions.addAll(plugin.getDungeonManager().getDungeonNames());
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}