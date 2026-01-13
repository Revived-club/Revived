package club.revived.lobby.game.parties;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Party
 *
 * @author yyuh
 * @since 11.01.26
 */
public final class Party {

    private UUID owner;
    private final List<UUID> members;
    private final List<UUID> bannedUsers;
    private boolean open = false;
    private boolean disbanded = false;

    public Party(UUID owner, List<UUID> members, List<UUID> bannedUsers) {
        this.owner = owner;
        this.members = members;
        this.bannedUsers = bannedUsers;
    }

    @NotNull
    public List<NetworkPlayer> players() {
        return PlayerManager.getInstance()
                .getNetworkPlayers()
                .values()
                .stream()
                .filter(networkPlayer -> this.members.contains(networkPlayer.getUuid()))
                .toList();
    }

    public void addMember(final UUID uuid) {
        this.members.add(uuid);
        this.update();
    }

    public void update() {
        Cluster.getInstance().getGlobalCache().removeFromList("parties", this, -1);
        for (final UUID member : this.members)  {
            if (!PlayerManager.getInstance().getNetworkPlayers().containsKey(member)) {
                continue;
            }

            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(member);
            networkPlayer.cacheValue(Party.class, this);
        }

        Cluster.getInstance().getGlobalCache().push("parties", this);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setDisbanded(boolean disbanded) {
        this.disbanded = disbanded;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public List<UUID> getBannedUsers() {
        return bannedUsers;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isDisbanded() {
        return disbanded;
    }
}