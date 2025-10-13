package my.pikrew.visantaraDungeonV2.models;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class Hologram {

    private final String id;
    private final Location location;
    private final List<String> lines;
    private final String dungeonName;
    private final List<ArmorStand> armorStands;

    public Hologram(String id, Location location, List<String> lines, String dungeonName) {
        this.id = id;
        this.location = location;
        this.lines = lines;
        this.dungeonName = dungeonName;
        this.armorStands = new ArrayList<>();
    }

    public void spawn() {
        if (location.getWorld() == null) {
            return;
        }

        double height = location.getY();

        for (int i = lines.size() - 1; i >= 0; i--) {
            Location standLoc = location.clone();
            standLoc.setY(height);

            ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCanPickupItems(false);
            stand.setCustomName(ChatColor.translateAlternateColorCodes('&', lines.get(i)));
            stand.setCustomNameVisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setCollidable(false);

            armorStands.add(stand);
            height += 0.25;
        }
    }

    public void remove() {
        for (ArmorStand stand : armorStands) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        armorStands.clear();
    }

    public void updateLines(List<String> newLines) {
        this.lines.clear();
        this.lines.addAll(newLines);
        remove();
        spawn();
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public List<String> getLines() {
        return lines;
    }

    public String getDungeonName() {
        return dungeonName;
    }

    public List<ArmorStand> getArmorStands() {
        return armorStands;
    }
}