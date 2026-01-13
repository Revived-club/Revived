package club.revived.lobby.game.parties;

import club.revived.commons.generic.StringUtils;
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

    private final String id;
    private UUID owner;
    private final List<UUID> members;
    private final List<UUID> bannedUsers;
    private boolean open;
    private boolean disbanded;

    public Party(
            final UUID owner,
            final List<UUID> members,
            final List<UUID> bannedUsers
    ) {
        this.id = StringUtils.generateId("#party-");
        this.owner = owner;
        this.members = members;
        this.bannedUsers = bannedUsers;

        final var cache = Cluster.getInstance().getGlobalCache();

        cache.push("parties", this.id, this);
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

    public String getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isDisbanded() {
        return disbanded;
    }

    public void save() {
        Cluster.getInstance()
                .getGlobalCache()
                .update("party:" + id, this);

        this.update();
    }

    public void disband() {
        this.disbanded = true;
        this.save();
    }

    public void addMember(final UUID uuid) {
        if (members.contains(uuid)) {
            return;
        }

        members.add(uuid);
        this.save();

        Cluster.getInstance().getGlobalCache()
                .set("player:" + uuid + ":party", id);
    }

    public void removeMember(final UUID uuid) {
        members.remove(uuid);
        this.save();

        Cluster.getInstance().getGlobalCache()
                .remove("player:" + uuid + ":party");
    }

    public void setOpen(final boolean open) {
        this.open = open;
        this.save();
    }

    public void setOwner(final UUID owner) {
        this.owner = owner;
        this.save();
    }

    public void update() {
        for (final UUID member : this.members) {
            if (!PlayerManager.getInstance().getNetworkPlayers().containsKey(member)) {
                continue;
            }
            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(member);
            networkPlayer.cacheValue(Party.class, this);
        }
    }
}