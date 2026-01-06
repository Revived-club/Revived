package club.revived.lobby.game.command;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.messaging.impl.PingRequest;
import club.revived.lobby.service.messaging.impl.PingResponse;
import dev.jorel.commandapi.CommandTree;

/**
 * PingCommand
 *
 * @author yyuh
 * @since 06.01.26
 */
public final class PingCommand {

    public PingCommand() {
        new CommandTree("ping-all")
                .executesPlayer((player, args) -> {
                    Cluster.getInstance().getMessagingService().sendGlobalRequest(new PingRequest(), PingResponse.class)
                            .thenAccept(pingResponse -> {
                                player.sendRichMessage(pingResponse.serverId() + " PONG");
                            });
                }).register("revived");
    }
}
