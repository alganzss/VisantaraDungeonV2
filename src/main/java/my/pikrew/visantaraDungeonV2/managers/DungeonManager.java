package my.pikrew.visantaraDungeonV2.managers;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import my.pikrew.visantaraDungeonV2.models.Dungeon;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DungeonManager {

    private final VisantaraDungeonV2 plugin;
    private final Map<String, Dungeon> dungeons;
    private final Map<UUID, String> playerInDungeon;
    private File dungeonsFile;
    private FileConfiguration dungeonsConfig;

    public DungeonManager(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        this.dungeons = new HashMap<>();
        this.playerInDungeon = new HashMap<>();
        loadDungeons();
    }

    public boolean createDungeon(String name) {
        if (dungeons.containsKey(name.toLowerCase())) {
            return false;
        }

        // Create dungeon world
        WorldCreator creator = new WorldCreator(name + "_dungeon");
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        World world = creator.createWorld();

        if (world == null) {
            return false;
        }

        // Set world rules
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setTime(6000);

        Location spawnLocation = world.getSpawnLocation();

        Dungeon dungeon = new Dungeon(name, world.getName(), spawnLocation);
        dungeons.put(name.toLowerCase(), dungeon);

        saveDungeons();
        return true;
    }

    public boolean deleteDungeon(String name) {
        if (!dungeons.containsKey(name.toLowerCase())) {
            return false;
        }

        Dungeon dungeon = dungeons.get(name.toLowerCase());
        World world = Bukkit.getWorld(dungeon.getWorldName());

        // Teleport all players out
        if (world != null) {
            for (Player player : world.getPlayers()) {
                exitDungeon(player);
            }

            Bukkit.unloadWorld(world, false);
        }

        dungeons.remove(name.toLowerCase());
        saveDungeons();
        return true;
    }

    public boolean enterDungeon(Player player, String dungeonName) {
        if (!dungeons.containsKey(dungeonName.toLowerCase())) {
            return false;
        }

        Dungeon dungeon = dungeons.get(dungeonName.toLowerCase());
        World world = Bukkit.getWorld(dungeon.getWorldName());

        if (world == null) {
            return false;
        }

        // Store player's current location
        plugin.getPlayerManager().setReturnLocation(player.getUniqueId(), player.getLocation());

        // Teleport to dungeon
        player.teleport(dungeon.getSpawnLocation());
        playerInDungeon.put(player.getUniqueId(), dungeonName.toLowerCase());

        // Update visibility with delay to ensure teleport is complete
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().updateAllVisibility();
        }, 5L);

        return true;
    }

    public void exitDungeon(Player player) {
        UUID playerId = player.getUniqueId();

        if (!playerInDungeon.containsKey(playerId)) {
            return;
        }

        Location returnLoc = plugin.getPlayerManager().getReturnLocation(playerId);
        if (returnLoc != null && returnLoc.getWorld() != null) {
            player.teleport(returnLoc);
        } else {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        playerInDungeon.remove(playerId);
        plugin.getPlayerManager().removeReturnLocation(playerId);

        // Update visibility with delay to ensure teleport is complete
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().updateAllVisibility();
        }, 5L);
    }

    public boolean isInDungeon(Player player) {
        return playerInDungeon.containsKey(player.getUniqueId());
    }

    public String getPlayerDungeon(UUID playerId) {
        return playerInDungeon.get(playerId);
    }

    public Dungeon getDungeon(String name) {
        return dungeons.get(name.toLowerCase());
    }

    public Set<String> getDungeonNames() {
        return dungeons.keySet();
    }

    public Map<String, Dungeon> getDungeons() {
        return dungeons;
    }

    private void loadDungeons() {
        dungeonsFile = new File(plugin.getDataFolder(), "dungeons.yml");
        if (!dungeonsFile.exists()) {
            try {
                dungeonsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dungeonsConfig = YamlConfiguration.loadConfiguration(dungeonsFile);

        if (dungeonsConfig.contains("dungeons")) {
            for (String key : dungeonsConfig.getConfigurationSection("dungeons").getKeys(false)) {
                String path = "dungeons." + key;
                String worldName = dungeonsConfig.getString(path + ".world");
                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    Location spawn = new Location(
                            world,
                            dungeonsConfig.getDouble(path + ".spawn.x"),
                            dungeonsConfig.getDouble(path + ".spawn.y"),
                            dungeonsConfig.getDouble(path + ".spawn.z"),
                            (float) dungeonsConfig.getDouble(path + ".spawn.yaw"),
                            (float) dungeonsConfig.getDouble(path + ".spawn.pitch")
                    );

                    Dungeon dungeon = new Dungeon(key, worldName, spawn);
                    dungeons.put(key.toLowerCase(), dungeon);
                }
            }
        }
    }

    public void saveAll() {
        saveDungeons();
    }

    private void saveDungeons() {
        for (Map.Entry<String, Dungeon> entry : dungeons.entrySet()) {
            String key = entry.getKey();
            Dungeon dungeon = entry.getValue();
            String path = "dungeons." + key;

            dungeonsConfig.set(path + ".world", dungeon.getWorldName());
            Location spawn = dungeon.getSpawnLocation();
            dungeonsConfig.set(path + ".spawn.x", spawn.getX());
            dungeonsConfig.set(path + ".spawn.y", spawn.getY());
            dungeonsConfig.set(path + ".spawn.z", spawn.getZ());
            dungeonsConfig.set(path + ".spawn.yaw", spawn.getYaw());
            dungeonsConfig.set(path + ".spawn.pitch", spawn.getPitch());
        }

        try {
            dungeonsConfig.save(dungeonsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}