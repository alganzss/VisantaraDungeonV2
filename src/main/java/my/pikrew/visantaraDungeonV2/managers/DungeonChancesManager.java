package my.pikrew.visantaraDungeonV2.managers;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonChancesManager {

    private final VisantaraDungeonV2 plugin;
    private final Map<UUID, Integer> playerChances;
    private final ScoreboardManager scoreboardManager;

    // Config values
    private int maxChances;
    private String scoreboardTitle;
    private String chanceDisplayName;
    private String dungeonName;
    private String serverInfo;

    public DungeonChancesManager(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        this.playerChances = new HashMap<>();
        this.scoreboardManager = Bukkit.getScoreboardManager();
        loadConfig();
    }

    private void loadConfig() {
        maxChances = plugin.getConfig().getInt("dungeon-chances.max-chances", 3);
        scoreboardTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("dungeon-chances.scoreboard.title", "&c&lDUNGEON"));
        chanceDisplayName = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("dungeon-chances.scoreboard.chance-display", "&eKesempatan: &f"));
        dungeonName = plugin.getConfig().getString("dungeon-chances.scoreboard.dungeon-name", "Wisantara Dungeon");
        serverInfo = plugin.getConfig().getString("dungeon-chances.scoreboard.server-info", "discord.wisantara.com");
    }

    public void reloadConfig() {
        loadConfig();
        // Update all scoreboards
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getDungeonManager().isInDungeon(player)) {
                updateScoreboard(player);
            }
        }
    }

    public void initializePlayer(Player player) {
        if (!playerChances.containsKey(player.getUniqueId())) {
            playerChances.put(player.getUniqueId(), maxChances);
        }
    }

    public void handlePlayerDeath(Player player) {
        // Cek apakah player mati di dungeon
        if (!plugin.getDungeonManager().isInDungeon(player)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        int currentChances = playerChances.getOrDefault(playerId, maxChances);

        if (currentChances > 0) {
            currentChances--;
            playerChances.put(playerId, currentChances);

            // Kirim pesan ke player
            player.sendMessage(ChatColor.RED + "You died in the dungeon! Lives remaining: " +
                    ChatColor.YELLOW + currentChances + ChatColor.RED + "/" + maxChances);

            if (currentChances == 0) {
                player.sendMessage(ChatColor.DARK_RED + "No lives left! You will be teleported out.");
            }

            updateScoreboard(player);
        }
    }

    public void handlePlayerRespawn(Player player) {
        UUID playerId = player.getUniqueId();
        int currentChances = playerChances.getOrDefault(playerId, maxChances);

        // Delay untuk memastikan respawn selesai
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Jika kesempatan habis dan player masih di dungeon, kick dan reset
            if (currentChances == 0 && plugin.getDungeonManager().isInDungeon(player)) {
                plugin.getDungeonManager().exitDungeon(player);
                playerChances.put(playerId, maxChances);
                player.sendMessage(ChatColor.GREEN + "Your lives have been reset! You can re-enter the dungeon.");
            }
            updateScoreboard(player);
        }, 10L);
    }

    public void updateScoreboard(Player player) {
        // Cek apakah player di dungeon
        if (!plugin.getDungeonManager().isInDungeon(player)) {
            // Hilangkan scoreboard jika tidak di dungeon
            player.setScoreboard(scoreboardManager.getMainScoreboard());
            return;
        }

        // Buat scoreboard baru
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("dungeonchances", "dummy", scoreboardTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int chances = playerChances.getOrDefault(player.getUniqueId(), maxChances);

        // Baris 15: Garis atas
        objective.getScore(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setScore(15);

        // Baris 14: Kosong
        objective.getScore("§r").setScore(14);

        // Baris 13: Nama dungeon
        String currentDungeon = plugin.getDungeonManager().getPlayerDungeon(player.getUniqueId());
        String displayDungeon = currentDungeon != null ? currentDungeon : dungeonName;
        objective.getScore(ChatColor.YELLOW + "⚔ " + ChatColor.WHITE + displayDungeon).setScore(13);

        // Baris 12: Kosong
        objective.getScore("§r ").setScore(12);

        // Baris 11: Lives dengan hearts
        String hearts = getHeartsDisplay(chances, maxChances);
        objective.getScore(ChatColor.RED + "❤ Lives: " + hearts).setScore(11);

        // Baris 10: Kesempatan numerik
        objective.getScore(ChatColor.GRAY + "   " + chances + "/" + maxChances + " remaining").setScore(10);

        // Baris 9: Kosong
        objective.getScore("§r  ").setScore(9);

        // Baris 8: Status
        String status = chances > 1 ? ChatColor.GREEN + "✓ Safe" :
                chances == 1 ? ChatColor.YELLOW + "⚠ Warning" :
                        ChatColor.RED + "✗ Critical";
        objective.getScore(status).setScore(8);

        // Baris 7: Kosong
        objective.getScore("§r   ").setScore(7);

        // Baris 6: Tips
        if (chances <= 1) {
            objective.getScore(ChatColor.RED + "⚡ Be careful!").setScore(6);
        } else {
            objective.getScore(ChatColor.AQUA + "⭐ Good luck!").setScore(6);
        }

        // Baris 5: Kosong
        objective.getScore("§r    ").setScore(5);

        // Baris 4: Server info
        objective.getScore(ChatColor.GRAY + serverInfo).setScore(4);

        // Baris 3: Kosong
        objective.getScore("§r     ").setScore(3);

        // Baris 2: Garis bawah
        objective.getScore(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setScore(2);

        // Set scoreboard ke player
        player.setScoreboard(scoreboard);
    }

    private String getHeartsDisplay(int current, int max) {
        StringBuilder hearts = new StringBuilder();

        // Hearts yang tersisa (merah)
        for (int i = 0; i < current; i++) {
            hearts.append(ChatColor.RED).append("♥");
        }

        // Hearts yang hilang (abu-abu)
        for (int i = current; i < max; i++) {
            hearts.append(ChatColor.GRAY).append("♥");
        }

        return hearts.toString();
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(scoreboardManager.getMainScoreboard());
    }

    public int getPlayerChances(UUID playerId) {
        return playerChances.getOrDefault(playerId, maxChances);
    }

    public void resetPlayerChances(UUID playerId) {
        playerChances.put(playerId, maxChances);
    }

    public void setPlayerChances(UUID playerId, int chances) {
        playerChances.put(playerId, Math.min(chances, maxChances));
    }

    public int getMaxChances() {
        return maxChances;
    }
}