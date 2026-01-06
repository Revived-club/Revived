package club.revived.lobby.game.command;

import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.service.player.NetworkPlayer;
import dev.jorel.commandapi.CommandTree;

/**
 * WhereIsCommand
 *
 * @author yyuh
 * @since 06.01.26
 */
public final class WhereIsCommand {

    public WhereIsCommand() {
        new CommandTree("whereis")
                .then(NetworkPlayerArgument.networkPlayer("target")
                        .executesPlayer((player, args) -> {
                            final var target = (NetworkPlayer) args.get("target");

                            target.whereIs().thenAccept(clusterService -> {
                                player.sendRichMessage(String.format("%s is on %s", target.getUsername(), clusterService.getId()));
                            });
                        })
                ).register("revived");
    }
}
