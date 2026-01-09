package club.revived.lobby.game.command;

import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.service.player.NetworkPlayer;
import dev.jorel.commandapi.CommandTree;

/**
 * WhereIsProxyCommand
 *
 * @author yyuh
 * @since 09.01.26
 */
public final class WhereIsProxyCommand {

    public WhereIsProxyCommand() {
        new CommandTree("whereis-proxy")
                .then(NetworkPlayerArgument.networkPlayer("target")
                        .executesPlayer((player, args) -> {
                            final var target = (NetworkPlayer) args.get("target");

                            target.whereIsProxy().thenAccept(clusterService -> {
                                player.sendRichMessage(String.format("<white>%s is on %s", target.getUsername(), clusterService.getId()));
                            });
                        })
                ).register("revived");
    }
}
