package club.revived.lobby.game.command;

import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.game.friends.FriendManager;
import club.revived.lobby.game.parties.PartyManager;
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
                .then(new LiteralArgument("add")
                        .then(NetworkPlayerArgument.networkPlayer("target")
                                .executesPlayer((player, args) -> {
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                                    final var target = (NetworkPlayer) args.get("target");

                                    FriendManager.getInstance().requestFriend(networkPlayer, target);
                                })))
                .then(new LiteralArgument("accept")
                        .executesPlayer((player, args) -> {
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                            FriendManager.getInstance().acceptFriendRequest(networkPlayer);
                        }))
                .register("revived");
    }
}
