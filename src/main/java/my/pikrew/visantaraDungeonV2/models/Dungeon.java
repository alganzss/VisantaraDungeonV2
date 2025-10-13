package my.pikrew.visantaraDungeonV2.models;

import org.bukkit.Location;

public class Dungeon {

    private final String name;
    private final String worldName;
    private Location spawnLocation;

    public Dungeon(String name, String worldName, Location spawnLocation) {
        this.name = name;
        this.worldName = worldName;
        this.spawnLocation = spawnLocation;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}
