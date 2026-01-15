package club.revived.lobby.game.friends;

import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;

import java.util.ArrayList;
import java.util.UUID;

/**
 * FriendManager
 *
 * @author yyuh
 * @since 15.01.26
 */
public final class FriendManager {

    private static FriendManager instance;

    public FriendManager() {
        instance = this;
    }

    public void acceptFriendRequest(final NetworkPlayer networkPlayer) {
        networkPlayer.getCachedValue(FriendRequest.class).thenAccept(friendRequest -> {
            if (friendRequest == null) {
                networkPlayer.sendMessage("<red>You don't have any open requests!");
                return;
            }

            final var sender = friendRequest.sender();

            this.addFriend(networkPlayer.getUuid(), sender);
            this.addFriend(sender, networkPlayer.getUuid());

            networkPlayer.sendMessage("<green>Scuessfully accepted friend reqst");
        });

    }

    public void requestFriend(
            final NetworkPlayer sender,
            final NetworkPlayer receiver
    ) {
        receiver.sendMessage("You got a  Friend request mate");

        final var request = new FriendRequest(
                sender.getUuid(),
                receiver.getUuid()
        );

        receiver.cacheExValue(
                FriendRequest.class,
                request,
                120
        );
    }

    public void addFriend(
            final UUID uuid,
            final UUID target
    ) {
        final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(uuid);

        networkPlayer.getCachedOrLoad(FriendHolder.class).thenAccept(friendHolder -> {

            if (friendHolder == null) {
                friendHolder = new FriendHolder(uuid, new ArrayList<>());
            }

            final var friends = friendHolder.friends();
            friends.add(new Friend(
                    target,
                    System.currentTimeMillis()
            ));

            DatabaseManager.getInstance().save(FriendHolder.class, friendHolder);
            networkPlayer.cacheValue(FriendHolder.class, friendHolder);
        });
    }

    public static FriendManager getInstance() {
        if (instance == null) {
            return new FriendManager();
        }

        return instance;
    }
}
