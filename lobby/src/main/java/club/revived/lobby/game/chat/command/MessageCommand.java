package club.revived.lobby.game.chat.command;

import club.revived.lobby.game.chat.MessageInfo;
import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

public final class MessageCommand {

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
