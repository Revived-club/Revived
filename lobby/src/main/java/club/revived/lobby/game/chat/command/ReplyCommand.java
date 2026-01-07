package club.revived.lobby.game.chat.command;

import club.revived.lobby.game.chat.MessageInfo;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

public final class ReplyCommand {
    
    private final PlayerManager playerManager = PlayerManager.getInstance();

    /**
     * Registers the "reply" (alias "r") chat command which sends a private message to the last conversation partner.
     *
     * <p>The command accepts a single `message` argument. When executed, it looks up the invoking player's
     * last MessageInfo; if no valid recipient is found or the recipient is not online, the invoker is notified.
     * Otherwise the command delivers a formatted mail-style message to the recipient and updates both participants'
     * MessageInfo caches with the current timestamp.</p>
     *
     * @implNote the command tree is registered under the "revived" command space
     */
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

                                final var sentMessage = String.format(
                                        "<#3B82F6>✉ <dark_grey>[<#3B82F6>%s → You<dark_grey>] <white>%s",
                                        player.getName(),
                                        message
                                );

                                target.sendMessage(sentMessage);
                                player.sendRichMessage(sentMessage);

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