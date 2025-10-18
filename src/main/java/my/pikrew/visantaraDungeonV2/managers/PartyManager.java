package my.pikrew.visantaraDungeonV2.managers;

import my.pikrew.visantaraDungeonV2.VisantaraDungeonV2;
import my.pikrew.visantaraDungeonV2.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyManager {

    private final VisantaraDungeonV2 plugin;
    private final Map<UUID, Party> parties; // partyId -> Party
    private final Map<UUID, UUID> playerParty; // playerId -> partyId
    private final Map<UUID, UUID> partyInvites; // playerId -> partyId (pending invites)

    public PartyManager(VisantaraDungeonV2 plugin) {
        this.plugin = plugin;
        this.parties = new HashMap<>();
        this.playerParty = new HashMap<>();
        this.partyInvites = new HashMap<>();
    }

    public Party createParty(UUID leaderId) {
        if (playerParty.containsKey(leaderId)) {
            return null; // Already in a party
        }

        Party party = new Party(leaderId);
        parties.put(party.getPartyId(), party);
        playerParty.put(leaderId, party.getPartyId());
        return party;
    }

    public boolean invitePlayer(UUID inviterId, UUID invitedId) {
        Party party = getPlayerParty(inviterId);
        if (party == null) {
            party = createParty(inviterId);
        }

        if (!party.isLeader(inviterId)) {
            return false; // Only leader can invite
        }

        if (party.isFull()) {
            return false; // Party is full
        }

        if (playerParty.containsKey(invitedId)) {
            return false; // Already in a party
        }

        partyInvites.put(invitedId, party.getPartyId());

        // Auto-expire invite after 60 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (partyInvites.containsKey(invitedId) && partyInvites.get(invitedId).equals(party.getPartyId())) {
                partyInvites.remove(invitedId);
            }
        }, 1200L); // 60 seconds

        return true;
    }

    public boolean acceptInvite(UUID playerId) {
        UUID partyId = partyInvites.remove(playerId);
        if (partyId == null) {
            return false; // No pending invite
        }

        Party party = parties.get(partyId);
        if (party == null || party.isFull()) {
            return false;
        }

        if (playerParty.containsKey(playerId)) {
            return false; // Already in a party
        }

        party.addMember(playerId);
        playerParty.put(playerId, partyId);

        // Update visibility for all party members
        plugin.getPlayerManager().updateAllVisibility();

        return true;
    }

    public boolean declineInvite(UUID playerId) {
        return partyInvites.remove(playerId) != null;
    }

    public boolean leaveParty(UUID playerId) {
        UUID partyId = playerParty.remove(playerId);
        if (partyId == null) {
            return false;
        }

        Party party = parties.get(partyId);
        if (party == null) {
            return false;
        }

        party.removeMember(playerId);

        // If leader left, transfer leadership or disband
        if (party.isLeader(playerId)) {
            if (party.getSize() > 0) {
                // Transfer to first member
                UUID newLeader = party.getMembers().iterator().next();
                party.setLeader(newLeader);
            } else {
                // Disband party
                parties.remove(partyId);
                return true;
            }
        }

        // If party is empty, remove it
        if (party.getSize() == 0) {
            parties.remove(partyId);
        }

        // Update visibility
        plugin.getPlayerManager().updateAllVisibility();

        return true;
    }

    public boolean kickPlayer(UUID kickerId, UUID targetId) {
        Party party = getPlayerParty(kickerId);
        if (party == null || !party.isLeader(kickerId)) {
            return false; // Not leader
        }

        if (!party.isMember(targetId) || party.isLeader(targetId)) {
            return false; // Can't kick yourself or non-member
        }

        party.removeMember(targetId);
        playerParty.remove(targetId);

        // Update visibility
        plugin.getPlayerManager().updateAllVisibility();

        return true;
    }

    public Party getPlayerParty(UUID playerId) {
        UUID partyId = playerParty.get(playerId);
        return partyId != null ? parties.get(partyId) : null;
    }

    public boolean hasPartyInvite(UUID playerId) {
        return partyInvites.containsKey(playerId);
    }

    public UUID getPendingInvite(UUID playerId) {
        return partyInvites.get(playerId);
    }

    public boolean isInSameParty(UUID player1, UUID player2) {
        Party party1 = getPlayerParty(player1);
        Party party2 = getPlayerParty(player2);

        return party1 != null && party2 != null && party1.getPartyId().equals(party2.getPartyId());
    }

    public void disbandParty(UUID leaderId) {
        Party party = getPlayerParty(leaderId);
        if (party == null || !party.isLeader(leaderId)) {
            return;
        }

        // Remove all members from party map
        for (UUID memberId : new HashSet<>(party.getMembers())) {
            playerParty.remove(memberId);
        }

        parties.remove(party.getPartyId());

        // Update visibility
        plugin.getPlayerManager().updateAllVisibility();
    }
}