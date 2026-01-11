package club.revived.lobby.game.parties;

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
public record Party(
        UUID owner,
        List<UUID> members,
        List<UUID> bannedUsers,
        boolean open
) {

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

        for (final UUID member : this.members)  {
            if (!PlayerManager.getInstance().getNetworkPlayers().containsKey(member)) {
                continue;
            }

            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(member);
            networkPlayer.cacheValue(Party.class, this);
        }
    }
}