package club.revived.lobby.game.command;

import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.game.parties.Party;
import club.revived.lobby.game.parties.PartyManager;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;

/**
 * PartyCommand
 *
 * @author yyuh
 * @since 11.01.26
 */
public final class PartyCommand {

    public PartyCommand() {
        new CommandTree("party")
                .then(new LiteralArgument("create")
                        .executesPlayer((player, _) -> {
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                            PartyManager.getInstance().make(networkPlayer);
                        }))
                .then(new LiteralArgument("invite")
                        .then(NetworkPlayerArgument.networkPlayer("target")
                                .executesPlayer((player, args) -> {
                                    final var target = (NetworkPlayer) args.get("target");
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                                    networkPlayer.getCachedValue(Party.class).thenAccept(party -> {
                                        if (party == null) {
                                            player.sendRichMessage("<red>You are not in a party!");
                                            return;
                                        }

                                        if (!party.owner().equals(player.getUniqueId())) {
                                            player.sendRichMessage("<red>You are not owner of the party!");
                                            return;
                                        }

                                        PartyManager.getInstance().request(
                                                networkPlayer,
                                                target,
                                                party
                                        );
                                    });
                                })))
                .then(new LiteralArgument("accept")
                        .executesPlayer((player, _) -> {
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                            PartyManager.getInstance().acceptRequest(networkPlayer);
                        }))
                .register("revived");
    }
}
