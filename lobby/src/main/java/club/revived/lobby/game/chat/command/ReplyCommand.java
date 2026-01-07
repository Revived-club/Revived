package club.revived.lobby.game.chat.command;

import club.revived.lobby.game.chat.MessageInfo;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

public final class ReplyCommand {
    
    private final PlayerManager playerManager = this.playerManager;

    public ReplyCommand() {
        new CommandTree("reply")
                .withAliases("r")
                .then(new GreedyStringArgument("message")
                        .executesPlayer((player, args) -> {
                            final var message = (String) args.get("message");
                            final var networkPlayer = this.playerManager.fromBukkitPlayer(player);

                            networkPlayer.getCachedValue(MessageInfo.class).thenAccept(messageInfo -> {
                                if (messageInfo == null) {
                                    networkPlayer.sendMessage("<red>You don't have anyone to respond to!");
                                    return;
                                }
                                
                                if (!this.playerManager.getNetworkPlayers().containsKey(messageInfo.sender())) {
                                    networkPlayer.sendMessage("<red>You don't have anyone to respond to!");
                                    return;
                                }

                                final var target = this.playerManager.fromBukkitPlayer(messageInfo.sender());

                                target.sendMessage(String.format(
                                        "<#3B82F6>✉ <dark_grey>[<#3B82F6>%s → You<dark_grey>] <white>%s",
                                        player.getName(),
                                        message
                                ));

                                target.cacheValue(MessageInfo.class, new MessageInfo(
                                        player.getUniqueId(),
                                        message,
                                        System.currentTimeMillis()
                                ));

                                networkPlayer.cacheValue(MessageInfo.class, new MessageInfo(
                                        target.getUuid(),
                                        message,
                                        System.currentTimeMillis()
                                ));
                            });
                        })
                ).register("revived");
    }
}
