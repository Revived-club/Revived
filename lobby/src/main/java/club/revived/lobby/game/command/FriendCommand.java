package club.revived.lobby.game.command;

import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.game.friends.FriendHolder;
import club.revived.lobby.game.friends.FriendManager;
import club.revived.lobby.game.inventory.FriendBrowserMenu;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;

/**
 * FriendCommand
 *
 * @author yyuh
 * @since 15.01.26
 */
public final class FriendCommand {

    public FriendCommand() {
        new CommandTree("friend")
                .then(new LiteralArgument("block")
                        .then(NetworkPlayerArgument.networkPlayer("target")
                                .executesPlayer(((player, args) -> {
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                                    final var target = (NetworkPlayer) args.get("target");

                                    if (target == null) {
                                        return;
                                    }

                                    networkPlayer.getCachedOrLoad(FriendHolder.class).thenAccept(friendHolder -> {
                                        friendHolder.blockList().add(target.getUuid());

                                        FriendManager.getInstance().removeFriend(networkPlayer, target.getUuid());
                                        DatabaseManager.getInstance().save(FriendHolder.class, friendHolder);
                                        networkPlayer.cacheValue(FriendHolder.class, friendHolder);
                                    });
                                })))
                        .then(new LiteralArgument("add")
                                .then(NetworkPlayerArgument.networkPlayer("target")
                                        .executesPlayer((player, args) -> {
                                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                                            final var target = (NetworkPlayer) args.get("target");


                                            if (target == null) {
                                                return;
                                            }

                                            target.getCachedValue(FriendHolder.class).thenAccept(friendHolder -> {
                                                if (friendHolder.blockList().contains(player.getUniqueId())) {
                                                    player.sendRichMessage(String.format("<red>%s blocked you!", target.getUsername()));
                                                    return;
                                                }

                                                FriendManager.getInstance().requestFriend(networkPlayer, target);
                                            });

                                        })))
                        .then(new LiteralArgument("list")
                                .executesPlayer((player, _) -> {
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                                    networkPlayer.getCachedOrLoad(FriendHolder.class).thenAccept(friendHolder -> {
                                        if (friendHolder == null) {
                                            networkPlayer.sendMessage("<red>You don't have any friends... ahhwww :(");
                                            return;
                                        }

                                        new FriendBrowserMenu(player, friendHolder);
                                    });
                                }))
                        .then(new LiteralArgument("accept")
                                .executesPlayer((player, _) -> {
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                                    FriendManager.getInstance().acceptFriendRequest(networkPlayer);
                                })))
                .register("revived");
    }
}
