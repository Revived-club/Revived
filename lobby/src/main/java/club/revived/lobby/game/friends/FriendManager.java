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

            networkPlayer.sendMessage("<green>You accepted the friend request!");

            if (PlayerManager.getInstance().isRegistered(sender)) {
                final var senderNetworkPlayer = PlayerManager.getInstance()
                        .fromBukkitPlayer(sender);

                senderNetworkPlayer.sendMessage(String.format("<green>%s accepted your friend request!", networkPlayer.getUsername()));
            }
        });

    }

    public void requestFriend(
            final NetworkPlayer sender,
            final NetworkPlayer receiver
    ) {
        sender.sendMessage("<green>Successfully sent the friend request!");

        receiver.sendMessage("<#3B82F6><player> <white>sent you a friend request! Accept the request by <click:run_command:'/friend accept'><hover:show_text:'<#3B82F6>Click to Accept'><#3B82F6>[Clicking Here]</hover></click>."
                .replace("<player>", sender.getUsername())
        );

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

    public void removeFriend(
            final NetworkPlayer player,
            final UUID target
    ) {
        player.getCachedOrLoad(FriendHolder.class).thenAccept(holder -> {
            if (holder == null || holder.friends().isEmpty()) {
                player.sendMessage("<red>You don't have any friends... ahhwww :(");
                return;
            }

            final boolean isFriend = holder.friends()
                    .stream()
                    .anyMatch(friend -> friend.uuid().equals(target));

            if (!isFriend) {
                player.sendMessage("<red>You are not friends with that player!");
                return;
            }

            final FriendHolder updatedSelf = new FriendHolder(
                    holder.uuid(),
                    holder.friends()
                            .stream()
                            .filter(friend -> !friend.uuid().equals(target))
                            .toList()
            );

            player.cacheValue(FriendHolder.class, updatedSelf);
            player.sendMessage("<green>Friend removed");

            if (!PlayerManager.getInstance().isRegistered(target)) {
                return;
            }

            final NetworkPlayer targetPlayer = PlayerManager.getInstance()
                    .fromBukkitPlayer(target);

            targetPlayer.getCachedOrLoad(FriendHolder.class).thenAccept(targetHolder -> {
                if (targetHolder == null) {
                    return;
                }

                final FriendHolder updatedTarget = new FriendHolder(
                        targetHolder.uuid(),
                        targetHolder.friends()
                                .stream()
                                .filter(friend -> !friend.uuid().equals(player.getUuid()))
                                .toList()
                );

                targetPlayer.cacheValue(FriendHolder.class, updatedTarget);
                targetPlayer.sendMessage(String.format("<red>You are no longer friends with %s", player.getUsername()));
            });
        });
    }

    public static FriendManager getInstance() {
        if (instance == null) {
            return new FriendManager();
        }

        return instance;
    }
}
