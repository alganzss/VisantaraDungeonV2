package my.pikrew.visantaraDungeonV2.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {

    private final UUID partyId;
    private UUID leader;
    private final Set<UUID> members;
    private final int maxSize;

    public Party(UUID leader) {
        this.partyId = UUID.randomUUID();
        this.leader = leader;
        this.members = new HashSet<>();
        this.members.add(leader);
        this.maxSize = 8; // Max 8 players per party
    }

    public UUID getPartyId() {
        return partyId;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean addMember(UUID playerId) {
        if (members.size() >= maxSize) {
            return false;
        }
        return members.add(playerId);
    }

    public boolean removeMember(UUID playerId) {
        return members.remove(playerId);
    }

    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    public boolean isLeader(UUID playerId) {
        return leader.equals(playerId);
    }

    public int getSize() {
        return members.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }
}
