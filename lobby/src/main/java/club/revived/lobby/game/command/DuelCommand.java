package club.revived.lobby.game.command;

import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.game.duel.DuelManager;
import club.revived.lobby.game.inventory.DuelRequestMenu;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;

/**
 * DuelCommand
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class DuelCommand {

    /**
     * Registers the "duel" command and its subcommands.
     *
     * The command provides:
     * - "duel accept": accepts a pending duel request for the executing player.
     * - "duel <target>": opens a duel request menu targeting the specified player.
     */
    public DuelCommand() {
        new CommandTree("duel")
                .then(new LiteralArgument("accept")
                        .executesPlayer((player, args) -> {
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                            DuelManager.getInstance().acceptDuelRequest(networkPlayer);
                        }))
                .then(NetworkPlayerArgument.networkPlayer("target")
                        .executesPlayer((player, args) -> {
                            final var networkPlayer = (NetworkPlayer) args.get("target");
                            new DuelRequestMenu(player, networkPlayer.getUuid());
                        })).register("revived");
    }
}