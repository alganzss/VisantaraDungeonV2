package my.pikrew.visantaraDungeonV2.Energy;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EnergyDisplayTask extends BukkitRunnable {

    private final VisantaraDungeonV2 plugin;
    private boolean enabled;
    private String format;
    private int barLength;
    private String filledHigh;
    private String filledMedium;
    private String filledLow;
    private String empty;
    private String brackets;

    public EnergyDisplayTask(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        enabled = plugin.getConfig().getBoolean("energy.display.enabled", true);
        format = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("energy.display.format", "&6⚡ Energi: &e{current} &7/ &e{max}  {bar}"));
        barLength = plugin.getConfig().getInt("energy.display.bar.length", 10);
        filledHigh = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("energy.display.bar.filled-high", "&a█"));
        filledMedium = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("energy.display.bar.filled-medium", "&e█"));
        filledLow = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("energy.display.bar.filled-low", "&c█"));
        empty = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("energy.display.bar.empty", "&8█"));
        brackets = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("energy.display.bar.brackets", "&7[]"));
    }

    @Override
    public void run() {
        if (!enabled) {
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player == null || !player.isOnline()) {
                continue;
            }

            // Get player energy
            Energy energy = plugin.getEnergyManager().getPlayerEnergy(player.getUniqueId());

            if (energy == null) {
                continue;
            }

            int current = energy.getCurrentEnergy();
            int max = energy.getMaxEnergy();

            // Create energy bar
            String energyBar = createEnergyBar(current, max);

            // Format message
            String message = format
                    .replace("{current}", String.valueOf(current))
                    .replace("{max}", String.valueOf(max))
                    .replace("{bar}", energyBar);

            // Send action bar
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }

    /**
     * Create energy bar with blocks
     * Example: [████████░░] (8/10)
     */
    private String createEnergyBar(int current, int max) {
        int filledBars = (int) Math.ceil(((double) current / max) * barLength);
        double percentage = (double) current / max;

        StringBuilder bar = new StringBuilder();

        // Add opening bracket
        if (brackets.length() >= 2) {
            bar.append(brackets.charAt(0));
        }

        // Filled portion
        for (int i = 0; i < filledBars; i++) {
            if (percentage >= 0.7) {
                bar.append(filledHigh);
            } else if (percentage >= 0.4) {
                bar.append(filledMedium);
            } else {
                bar.append(filledLow);
            }
        }

        // Empty portion
        for (int i = filledBars; i < barLength; i++) {
            bar.append(empty);
        }

        // Add closing bracket
        if (brackets.length() >= 2) {
            bar.append(brackets.charAt(1));
        }

        return bar.toString();
    }

    /**
     * Reload configuration
     */
    public void reloadConfig() {
        loadConfig();
    }
}