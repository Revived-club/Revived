package club.revived.lobby.game.chat.command;

import club.revived.lobby.game.chat.MessageInfo;
import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

public final class MessageCommand {

    /**
     * Registers the "msg" chat command which delivers a private message and records message metadata for sender and recipient.
     *
     * <p>The command accepts a target {@code NetworkPlayer} and a message string. When executed by a player, it sends a formatted
     * private message to the target and caches a {@code MessageInfo} on both the recipient and the sender containing the
     * counterpart's UUID, the message text, and the current timestamp. The command tree is registered under the name "revived".</p>
     */
    public MessageCommand() {
        new CommandTree("msg")
                .then(NetworkPlayerArgument.networkPlayer("target")
                        .then(new GreedyStringArgument("message")
                                .executesPlayer((player, args) -> {
                                    final var networkPlayer = (NetworkPlayer) args.get("target");
                                    final var message = (String) args.get("message");

                                    networkPlayer.sendMessage(String.format(
                                            "<#3B82F6>✉ <dark_grey>[<#3B82F6>%s → You<dark_grey>] <white>%s",
                                            player.getName(),
                                            message
                                    ));

                                    networkPlayer.cacheValue(MessageInfo.class, new MessageInfo(
                                            player.getUniqueId(),
                                            message,
                                            System.currentTimeMillis()
                                    ));

                                    final var sender = PlayerManager.getInstance().fromBukkitPlayer(player);

                                    sender.cacheValue(MessageInfo.class, new MessageInfo(
                                            networkPlayer.getUuid(),
                                            message,
                                            System.currentTimeMillis()
                                    ));
                                })
                        )).register("revived");
    }
}