package club.revived.lobby.game.command;

import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.service.player.NetworkPlayer;
import dev.jorel.commandapi.CommandTree;

import java.util.concurrent.TimeUnit;

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
                                player.sendRichMessage(String.format("<white>%s is on %s", target.getUsername(), clusterService.getId()));
                            }).orTimeout(3, TimeUnit.SECONDS).thenAccept(v -> {
                                player.sendRichMessage(String.format("<red>Timed out while trying to find %s", target.getUsername()));
                            });
                        })
                ).register("revived");
    }
}
