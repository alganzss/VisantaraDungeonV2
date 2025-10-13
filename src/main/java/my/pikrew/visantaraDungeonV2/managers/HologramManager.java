package my.pikrew.visantaraDungeonV2.managers;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import my.pikrew.visantaraDungeonV2.models.Hologram;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HologramManager {

    private final VisantaraDungeonV2 plugin;
    private final Map<String, Hologram> holograms;
    private File hologramsFile;
    private FileConfiguration hologramsConfig;

    public HologramManager(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
        loadHolograms();
    }

    public boolean createHologram(String id, Location location, List<String> lines, String dungeonName) {
        if (holograms.containsKey(id.toLowerCase())) {
            return false;
        }

        Hologram hologram = new Hologram(id, location, lines, dungeonName);
        hologram.spawn();

        holograms.put(id.toLowerCase(), hologram);
        saveHolograms();
        return true;
    }

    public boolean deleteHologram(String id) {
        if (!holograms.containsKey(id.toLowerCase())) {
            return false;
        }

        Hologram hologram = holograms.get(id.toLowerCase());
        hologram.remove();
        holograms.remove(id.toLowerCase());
        saveHolograms();
        return true;
    }

    public Hologram getHologram(String id) {
        return holograms.get(id.toLowerCase());
    }

    public Hologram getHologramByEntity(ArmorStand stand) {
        for (Hologram hologram : holograms.values()) {
            if (hologram.getArmorStands().contains(stand)) {
                return hologram;
            }
        }
        return null;
    }

    public Set<String> getHologramIds() {
        return holograms.keySet();
    }

    public void removeAllHolograms() {
        for (Hologram hologram : holograms.values()) {
            hologram.remove();
        }
    }

    public void respawnAllHolograms() {
        for (Hologram hologram : holograms.values()) {
            hologram.remove();
            hologram.spawn();
        }
    }

    private void loadHolograms() {
        hologramsFile = new File(plugin.getDataFolder(), "holograms.yml");
        if (!hologramsFile.exists()) {
            try {
                hologramsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        hologramsConfig = YamlConfiguration.loadConfiguration(hologramsFile);

        if (hologramsConfig.contains("holograms")) {
            for (String key : hologramsConfig.getConfigurationSection("holograms").getKeys(false)) {
                String path = "holograms." + key;

                String worldName = hologramsConfig.getString(path + ".world");
                if (plugin.getServer().getWorld(worldName) == null) {
                    continue;
                }

                Location location = new Location(
                        plugin.getServer().getWorld(worldName),
                        hologramsConfig.getDouble(path + ".x"),
                        hologramsConfig.getDouble(path + ".y"),
                        hologramsConfig.getDouble(path + ".z")
                );

                List<String> lines = hologramsConfig.getStringList(path + ".lines");
                String dungeonName = hologramsConfig.getString(path + ".dungeon");

                Hologram hologram = new Hologram(key, location, lines, dungeonName);
                hologram.spawn();
                holograms.put(key.toLowerCase(), hologram);
            }
        }
    }

    private void saveHolograms() {
        hologramsConfig.set("holograms", null);

        for (Map.Entry<String, Hologram> entry : holograms.entrySet()) {
            String key = entry.getKey();
            Hologram hologram = entry.getValue();
            String path = "holograms." + key;

            Location loc = hologram.getLocation();
            hologramsConfig.set(path + ".world", loc.getWorld().getName());
            hologramsConfig.set(path + ".x", loc.getX());
            hologramsConfig.set(path + ".y", loc.getY());
            hologramsConfig.set(path + ".z", loc.getZ());
            hologramsConfig.set(path + ".lines", hologram.getLines());
            hologramsConfig.set(path + ".dungeon", hologram.getDungeonName());
        }

        try {
            hologramsConfig.save(hologramsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
