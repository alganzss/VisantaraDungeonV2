package my.pikrew.visantaraDungeonV2.models;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Hologram {

    private final String id;
    private final Location location;
    private final List<String> lines;
    private final String dungeonName;
    private final List<ArmorStand> armorStands;
    private Slime clickableEntity; // Entity yang bisa diklik

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

        // Spawn clickable invisible entity di tengah hologram
        Location clickLoc = location.clone();
        clickLoc.add(0, 0.5, 0); // Sedikit di atas ground

        clickableEntity = (Slime) location.getWorld().spawnEntity(clickLoc, EntityType.SLIME);
        clickableEntity.setSize(2); // Size 2 = medium size
        clickableEntity.setAI(false);
        clickableEntity.setGravity(false);
        clickableEntity.setInvulnerable(true);
        clickableEntity.setSilent(true);
        clickableEntity.setCollidable(false);
        clickableEntity.setCustomName(ChatColor.GREEN + "Click to Enter");
        clickableEntity.setCustomNameVisible(false);

        // Make invisible with potion effect
        clickableEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                1,
                false,
                false
        ));

        // Prevent entity from moving
        clickableEntity.setVelocity(clickableEntity.getVelocity().zero());

        // Spawn armor stands untuk text
        double height = location.getY() + 2.5; // Start dari atas clickable entity

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
        // Remove clickable entity
        if (clickableEntity != null && !clickableEntity.isDead()) {
            clickableEntity.remove();
        }

        // Remove armor stands
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

    public Slime getClickableEntity() {
        return clickableEntity;
    }

    public boolean isClickableEntity(Entity entity) {
        return clickableEntity != null && clickableEntity.equals(entity);
    }
}