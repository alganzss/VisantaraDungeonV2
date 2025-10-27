package my.pikrew.visantaraDungeonV2.Energy;

import java.util.UUID;

public class Energy {

    private final UUID playerId;
    private int currentEnergy;
    private long lastUpdate;
    private final int maxEnergy;

    // 5 minutes in milliseconds
    private static final long REGENERATION_TIME = 5 * 60 * 1000;

    public Energy(UUID playerId) {
        this(playerId, 100, System.currentTimeMillis());
    }

    public Energy(UUID playerId, int currentEnergy, long lastUpdate) {
        this.playerId = playerId;
        this.currentEnergy = currentEnergy;
        this.lastUpdate = lastUpdate;
        this.maxEnergy = 100;
    }

    /**
     * Update energy based on time passed
     */
    public void updateEnergy() {
        if (currentEnergy >= maxEnergy) {
            lastUpdate = System.currentTimeMillis();
            return;
        }

        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastUpdate;

        // Calculate how many energy points should be regenerated
        int energyToAdd = (int) (timePassed / REGENERATION_TIME);

        if (energyToAdd > 0) {
            currentEnergy = Math.min(currentEnergy + energyToAdd, maxEnergy);
            // Update last update time, keeping track of remaining time
            lastUpdate += (energyToAdd * REGENERATION_TIME);
        }
    }

    /**
     * Consume energy
     * @param amount Amount to consume
     * @return true if successful, false if not enough energy
     */
    public boolean consumeEnergy(int amount) {
        updateEnergy();

        if (currentEnergy >= amount) {
            currentEnergy -= amount;
            lastUpdate = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Add energy (for admin commands or special cases)
     */
    public void addEnergy(int amount) {
        updateEnergy();
        currentEnergy = Math.min(currentEnergy + amount, maxEnergy);
    }

    /**
     * Get current energy (updates before returning)
     */
    public int getCurrentEnergy() {
        updateEnergy();
        return currentEnergy;
    }

    /**
     * Get time until next energy point (in milliseconds)
     */
    public long getTimeUntilNextEnergy() {
        if (currentEnergy >= maxEnergy) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastUpdate;
        long timeRemaining = REGENERATION_TIME - (timePassed % REGENERATION_TIME);

        return timeRemaining;
    }

    /**
     * Get time until next energy in formatted string
     */
    public String getTimeUntilNextEnergyFormatted() {
        long milliseconds = getTimeUntilNextEnergy();

        if (milliseconds == 0) {
            return "Full";
        }

        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;

        return String.format("%dm %ds", minutes, seconds);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setCurrentEnergy(int energy) {
        this.currentEnergy = Math.min(energy, maxEnergy);
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}