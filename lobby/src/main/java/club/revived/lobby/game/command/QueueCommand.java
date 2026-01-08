package club.revived.lobby.game.command;

import club.revived.lobby.game.duel.DuelManager;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.duel.QueueType;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;

/**
 * QueueCommand
 *
 * @author yyuh
 * @since 08.01.26
 */
public final class QueueCommand {

    public QueueCommand() {
        new CommandTree("queue")
                .executesPlayer(((sender, args) -> {
                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(sender);

                    DuelManager.getInstance().queue(
                            networkPlayer,
                            KitType.SWORD,
                            QueueType.SOLO
                    );

                })).register("revived");
    }
}
