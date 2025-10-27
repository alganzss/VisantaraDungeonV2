package my.pikrew.visantaraDungeonV2.Energy;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PrizeManager {

    private final VisantaraDungeonV2 plugin;
    private final Map<UUID, List<ItemStack>> playerPrizes;
    private final List<PrizeItem> availablePrizes;
    private final Random random;

    public PrizeManager(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        this.playerPrizes = new HashMap<>();
        this.availablePrizes = new ArrayList<>();
        this.random = new Random();
        loadPrizesFromConfig();
    }

    private void loadPrizesFromConfig() {
        ConfigurationSection prizesSection = plugin.getConfig().getConfigurationSection("prizes.items");

        if (prizesSection == null) {
            plugin.getLogger().warning("No prizes configured! Using default prizes.");
            addDefaultPrizes();
            return;
        }

        for (String key : prizesSection.getKeys(false)) {
            String path = "prizes.items." + key;

            try {
                Material material = Material.valueOf(plugin.getConfig().getString(path + ".material", "DIAMOND"));
                int amount = plugin.getConfig().getInt(path + ".amount", 1);
                String name = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString(path + ".name", "Prize"));
                List<String> lore = plugin.getConfig().getStringList(path + ".lore");
                int weight = plugin.getConfig().getInt(path + ".weight", 100);

                // Translate lore colors
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }

                availablePrizes.add(new PrizeItem(material, amount, name, coloredLore, weight));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load prize: " + key + " - " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + availablePrizes.size() + " prizes from config.");

        if (availablePrizes.isEmpty()) {
            addDefaultPrizes();
        }
    }

    private void addDefaultPrizes() {
        availablePrizes.add(new PrizeItem(Material.DIAMOND, 5,
                ChatColor.AQUA + "Diamonds",
                Arrays.asList(ChatColor.GRAY + "Shiny diamonds!"), 100));

        availablePrizes.add(new PrizeItem(Material.EMERALD, 10,
                ChatColor.GREEN + "Emeralds",
                Arrays.asList(ChatColor.GRAY + "Village currency!"), 80));

        availablePrizes.add(new PrizeItem(Material.GOLDEN_APPLE, 3,
                ChatColor.GOLD + "Golden Apples",
                Arrays.asList(ChatColor.GRAY + "Delicious and healthy!"), 50));

        availablePrizes.add(new PrizeItem(Material.ENCHANTED_BOOK, 1,
                ChatColor.LIGHT_PURPLE + "Enchanted Book",
                Arrays.asList(ChatColor.GRAY + "Random enchantment!"), 30));
    }

    public void openPrizeGUI(Player player) {
        // Generate 3 random prizes if not already generated
        if (!playerPrizes.containsKey(player.getUniqueId())) {
            rerollPrizes(player.getUniqueId());
        }

        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.GOLD + "✦ " + ChatColor.YELLOW + "Prize Selection" + ChatColor.GOLD + " ✦");

        // Get player's current prizes
        List<ItemStack> prizes = playerPrizes.get(player.getUniqueId());

        // Set prize items (slots 11, 13, 15)
        gui.setItem(11, addPrizeGlow(prizes.get(0)));
        gui.setItem(13, addPrizeGlow(prizes.get(1)));
        gui.setItem(15, addPrizeGlow(prizes.get(2)));

        // Reroll button
        ItemStack rerollItem = new ItemStack(Material.ARROW);
        ItemMeta rerollMeta = rerollItem.getItemMeta();
        rerollMeta.setDisplayName(ChatColor.YELLOW + "⟳ Reroll Prizes");

        int rerollCost = plugin.getEnergyManager().getPrizeRerollCost();
        int currentEnergy = plugin.getEnergyManager().getPlayerEnergy(player.getUniqueId()).getCurrentEnergy();

        rerollMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to get new prizes!",
                "",
                ChatColor.GOLD + "Cost: " + ChatColor.WHITE + rerollCost + " Energy",
                currentEnergy >= rerollCost ?
                        ChatColor.GREEN + "✓ You have enough energy" :
                        ChatColor.RED + "✗ Not enough energy",
                "",
                ChatColor.DARK_GRAY + "Current Energy: " + currentEnergy
        ));
        rerollItem.setItemMeta(rerollMeta);
        gui.setItem(22, rerollItem);

        // Info item
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "ℹ How to Claim");

        int claimCost = plugin.getEnergyManager().getPrizeClaimCost();

        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click on any prize above",
                ChatColor.GRAY + "to claim it!",
                "",
                ChatColor.GOLD + "Claim Cost: " + ChatColor.WHITE + claimCost + " Energy",
                ChatColor.GOLD + "Reroll Cost: " + ChatColor.WHITE + rerollCost + " Energy",
                "",
                ChatColor.YELLOW + "Your Energy: " + ChatColor.WHITE + currentEnergy + ChatColor.GRAY + "/100"
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);

        // Close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "✖ Close");
        closeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to close"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(18, closeItem);

        // Fill empty slots with glass pane
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    private ItemStack addPrizeGlow(ItemStack item) {
        ItemStack glowItem = item.clone();
        ItemMeta meta = glowItem.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Add claim instruction to lore
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GREEN + "▶ Click to claim this prize!");
        lore.add(ChatColor.GOLD + "Cost: " + ChatColor.WHITE +
                plugin.getEnergyManager().getPrizeClaimCost() + " Energy");
        meta.setLore(lore);

        glowItem.setItemMeta(meta);
        return glowItem;
    }

    public void rerollPrizes(UUID playerId) {
        List<ItemStack> newPrizes = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            PrizeItem prize = getRandomPrize();
            newPrizes.add(prize.createItemStack());
        }

        playerPrizes.put(playerId, newPrizes);
    }

    private PrizeItem getRandomPrize() {
        int totalWeight = availablePrizes.stream().mapToInt(p -> p.weight).sum();
        int randomWeight = random.nextInt(totalWeight);

        int currentWeight = 0;
        for (PrizeItem prize : availablePrizes) {
            currentWeight += prize.weight;
            if (randomWeight < currentWeight) {
                return prize;
            }
        }

        return availablePrizes.get(0); // Fallback
    }

    public boolean claimPrize(Player player, int slot) {
        List<ItemStack> prizes = playerPrizes.get(player.getUniqueId());
        if (prizes == null) {
            return false;
        }

        int prizeIndex = -1;
        if (slot == 11) prizeIndex = 0;
        else if (slot == 13) prizeIndex = 1;
        else if (slot == 15) prizeIndex = 2;

        if (prizeIndex == -1 || prizeIndex >= prizes.size()) {
            return false;
        }

        ItemStack prize = prizes.get(prizeIndex);

        // Remove glow and claim instruction from lore
        ItemStack cleanPrize = prize.clone();
        ItemMeta meta = cleanPrize.getItemMeta();
        meta.removeEnchant(Enchantment.LURE);

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            // Remove last 3 lines (claim instruction)
            if (lore.size() >= 3) {
                lore = lore.subList(0, lore.size() - 3);
                meta.setLore(lore);
            }
        }

        cleanPrize.setItemMeta(meta);

        // Give prize to player
        player.getInventory().addItem(cleanPrize);

        // Clear player's prizes
        playerPrizes.remove(player.getUniqueId());

        return true;
    }

    public void clearPlayerPrizes(UUID playerId) {
        playerPrizes.remove(playerId);
    }

    private static class PrizeItem {
        private final Material material;
        private final int amount;
        private final String name;
        private final List<String> lore;
        private final int weight;

        public PrizeItem(Material material, int amount, String name, List<String> lore, int weight) {
            this.material = material;
            this.amount = amount;
            this.name = name;
            this.lore = lore;
            this.weight = weight;
        }

        public ItemStack createItemStack() {
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }
    }
}