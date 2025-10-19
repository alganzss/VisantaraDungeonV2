package my.pikrew.visantaraDungeonV2.commands;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import my.pikrew.visantaraDungeonV2.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartyCommand implements CommandExecutor, TabCompleter {

    private final VisantaraDungeonV2 plugin;

    public PartyCommand(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player);
            case "invite":
                return handleInvite(player, args);
            case "accept":
                return handleAccept(player);
            case "decline":
                return handleDecline(player);
            case "leave":
                return handleLeave(player);
            case "kick":
                return handleKick(player, args);
            case "view":
            case "list":
                return handleView(player);
            case "disband":
                return handleDisband(player);
            default:
                sendHelp(player);
                return true;
        }
    }

    private boolean handleCreate(Player player) {
        if (plugin.getPartyManager().getPlayerParty(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "You are already in a party!");
            return true;
        }

        Party party = plugin.getPartyManager().createParty(player.getUniqueId());
        if (party != null) {
            player.sendMessage(ChatColor.GREEN + "Party created! You are the leader.");
            player.sendMessage(ChatColor.YELLOW + "Use /party invite <player> to invite members.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create party!");
        }

        return true;
    }

    private boolean handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /party invite <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot invite yourself!");
            return true;
        }

        if (plugin.getPartyManager().invitePlayer(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Invited " + target.getName() + " to your party!");

            target.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
            target.sendMessage(ChatColor.YELLOW + "You have been invited to " + player.getName() + "'s party!");
            target.sendMessage(ChatColor.GREEN + "/party accept" + ChatColor.GRAY + " - Accept invitation");
            target.sendMessage(ChatColor.RED + "/party decline" + ChatColor.GRAY + " - Decline invitation");
            target.sendMessage(ChatColor.DARK_GRAY + "This invitation will expire in 60 seconds.");
            target.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        } else {
            Party party = plugin.getPartyManager().getPlayerParty(player.getUniqueId());
            if (party == null) {
                player.sendMessage(ChatColor.RED + "You are not in a party! Use /party create first.");
            } else if (!party.isLeader(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Only the party leader can invite players!");
            } else if (party.isFull()) {
                player.sendMessage(ChatColor.RED + "Your party is full! (Max: " + party.getMaxSize() + " players)");
            } else if (plugin.getPartyManager().getPlayerParty(target.getUniqueId()) != null) {
                player.sendMessage(ChatColor.RED + target.getName() + " is already in a party!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to invite player!");
            }
        }

        return true;
    }

    private boolean handleAccept(Player player) {
        if (!plugin.getPartyManager().hasPartyInvite(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have no pending party invitations!");
            return true;
        }

        final UUID partyId = plugin.getPartyManager().getPendingInvite(player.getUniqueId());
        final Party party = plugin.getPartyManager().getPartyById(partyId);

        if (party == null) {
            player.sendMessage(ChatColor.RED + "Party no longer exists!");
            plugin.getPartyManager().declineInvite(player.getUniqueId());
            return true;
        }

        if (plugin.getPartyManager().acceptInvite(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "You joined the party!");

            // Notify all party members
            for (UUID memberId : party.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && !member.equals(player)) {
                    member.sendMessage(ChatColor.YELLOW + player.getName() + " joined the party!");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to join party! The party might be full.");
        }

        return true;
    }

    private boolean handleDecline(Player player) {
        if (plugin.getPartyManager().declineInvite(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You declined the party invitation.");
        } else {
            player.sendMessage(ChatColor.RED + "You have no pending party invitations!");
        }

        return true;
    }

    private boolean handleLeave(Player player) {
        Party party = plugin.getPartyManager().getPlayerParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return true;
        }

        boolean wasLeader = party.isLeader(player.getUniqueId());

        if (plugin.getPartyManager().leaveParty(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You left the party.");

            // Notify remaining members
            for (UUID memberId : party.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage(ChatColor.YELLOW + player.getName() + " left the party.");
                    if (wasLeader && party.getSize() > 0) {
                        UUID newLeader = party.getLeader();
                        if (member.getUniqueId().equals(newLeader)) {
                            member.sendMessage(ChatColor.GREEN + "You are now the party leader!");
                        }
                    }
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to leave party!");
        }

        return true;
    }

    private boolean handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /party kick <player>");
            return true;
        }

        Party party = plugin.getPartyManager().getPlayerParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return true;
        }

        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the party leader can kick members!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (plugin.getPartyManager().kickPlayer(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Kicked " + target.getName() + " from the party.");
            target.sendMessage(ChatColor.RED + "You have been kicked from the party!");

            // Notify other members
            for (UUID memberId : party.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && !member.equals(player) && !member.equals(target)) {
                    member.sendMessage(ChatColor.YELLOW + target.getName() + " was kicked from the party.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to kick player!");
        }

        return true;
    }

    private boolean handleView(Player player) {
        Party party = plugin.getPartyManager().getPlayerParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            player.sendMessage(ChatColor.YELLOW + "Use /party create to create one!");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Party Members " +
                ChatColor.GRAY + "(" + party.getSize() + "/" + party.getMaxSize() + ")");
        player.sendMessage("");

        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            String status = member != null ? ChatColor.GREEN + "●" : ChatColor.RED + "●";
            String name = member != null ? member.getName() : "Unknown";

            if (party.isLeader(memberId)) {
                player.sendMessage(status + " " + ChatColor.GOLD + "★ " + ChatColor.YELLOW + name +
                        ChatColor.GRAY + " (Leader)");
            } else {
                player.sendMessage(status + " " + ChatColor.WHITE + name);
            }
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");

        return true;
    }

    private boolean handleDisband(Player player) {
        Party party = plugin.getPartyManager().getPlayerParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return true;
        }

        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the party leader can disband the party!");
            return true;
        }

        // Notify all members
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage(ChatColor.RED + "The party has been disbanded!");
            }
        }

        plugin.getPartyManager().disbandParty(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Party disbanded.");

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "========== Party Commands ==========");
        player.sendMessage(ChatColor.YELLOW + "/party create" + ChatColor.GRAY + " - Create a party");
        player.sendMessage(ChatColor.YELLOW + "/party invite <player>" + ChatColor.GRAY + " - Invite a player");
        player.sendMessage(ChatColor.YELLOW + "/party accept" + ChatColor.GRAY + " - Accept invitation");
        player.sendMessage(ChatColor.YELLOW + "/party decline" + ChatColor.GRAY + " - Decline invitation");
        player.sendMessage(ChatColor.YELLOW + "/party leave" + ChatColor.GRAY + " - Leave the party");
        player.sendMessage(ChatColor.YELLOW + "/party kick <player>" + ChatColor.GRAY + " - Kick a member");
        player.sendMessage(ChatColor.YELLOW + "/party view" + ChatColor.GRAY + " - View party members");
        player.sendMessage(ChatColor.YELLOW + "/party disband" + ChatColor.GRAY + " - Disband the party");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "invite", "accept", "decline",
                    "leave", "kick", "view", "disband"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("kick")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}