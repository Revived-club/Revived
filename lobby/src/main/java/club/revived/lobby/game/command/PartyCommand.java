package club.revived.lobby.game.command;

import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.game.parties.Party;
import club.revived.lobby.game.parties.PartyManager;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
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
                .then(new LiteralArgument("join")
                        .then(NetworkPlayerArgument.networkPlayer("target")
                                .executesPlayer((player, args) -> {
                                    final var target = (NetworkPlayer) args.get("target");
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                                    target.getCachedValue(Party.class).thenAccept(party -> {
                                        if (party == null) {
                                            networkPlayer.sendMessage(String.format("<red>%s is not in a party", target.getUsername()));
                                            return;
                                        }

                                        if (!party.isOpen()) {
                                            networkPlayer.sendMessage(String.format("<red>%s's party is not open!", target.getUsername()));
                                            return;
                                        }

                                        if (party.isDisbanded()) {
                                            networkPlayer.sendMessage(String.format("<red>%s's party is disbanded!", target.getUsername()));
                                            return;
                                        }

                                        for (final var uuid : party.getMembers()) {
                                            if (!PlayerManager.getInstance().getNetworkPlayers().containsKey(uuid)) {
                                                continue;
                                            }

                                            final var member = PlayerManager.getInstance().fromBukkitPlayer(uuid);
                                            member.sendMessage(String.format("%s joined the party!", networkPlayer.getUsername()));
                                        }

                                        party.addMember(networkPlayer.getUuid());
                                    });
                                })))
                .then(new LiteralArgument("set-public")
                        .then(new BooleanArgument("state")
                                .executesPlayer((player, args) -> {
                                    final var bool = (boolean) args.get("state");
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                                    networkPlayer.getCachedValue(Party.class).thenAccept(party -> {
                                        if (party == null) {
                                            player.sendRichMessage("<red>You are not in a party!");
                                            return;
                                        }

                                        if (!party.getOwner().equals(player.getUniqueId())) {
                                            player.sendRichMessage("<red>You are not owner of the party!");
                                            return;
                                        }

                                        party.setOpen(bool);
                                        party.update();
                                    });
                                })))
                .then(new LiteralArgument("kick")
                        .then(NetworkPlayerArgument.networkPlayer("target")
                                .executesPlayer((player, args) -> {
                                    final var target = (NetworkPlayer) args.get("target");
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                                    networkPlayer.getCachedValue(Party.class).thenAccept(party -> {
                                        if (party == null) {
                                            player.sendRichMessage("<red>You are not in a party!");
                                            return;
                                        }

                                        if (!party.getOwner().equals(player.getUniqueId())) {
                                            player.sendRichMessage("<red>You are not owner of the party!");
                                            return;
                                        }

                                        if (!party.getMembers().contains(target.getUuid())) {
                                            player.sendRichMessage(String.format("<red>%s is not in the party!", target.getUsername()));
                                            return;
                                        }

                                        PartyManager.getInstance().kick(party, target.getUuid());
                                    });
                                })))
                .then(new LiteralArgument("transfer")
                        .then(NetworkPlayerArgument.networkPlayer("target")
                                .executesPlayer((player, args) -> {
                                    final var target = (NetworkPlayer) args.get("target");
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                                    networkPlayer.getCachedValue(Party.class).thenAccept(party -> {
                                        if (party == null) {
                                            player.sendRichMessage("<red>You are not in a party!");
                                            return;
                                        }

                                        if (!party.getOwner().equals(player.getUniqueId())) {
                                            player.sendRichMessage("<red>You are not owner of the party!");
                                            return;
                                        }

                                        if (!party.getMembers().contains(target.getUuid())) {
                                            player.sendRichMessage(String.format("<red>%s is not in the party!", target.getUsername()));
                                            return;
                                        }

                                        PartyManager.getInstance().transferOwnership(party, target.getUuid());
                                    });
                                })))
                .then(new LiteralArgument("chat")
                        .then(new GreedyStringArgument("message")
                                .executesPlayer((player, args) -> {
                                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                                    final String message = (String) args.get("message");

                                    networkPlayer.getCachedValue(Party.class).thenAccept(party -> {
                                        if (party == null) {
                                            player.sendRichMessage("<red>You are not in a party!");
                                            return;
                                        }

                                        for (final var uuid : party.getMembers()) {
                                            if (!PlayerManager.getInstance().isRegistered(uuid)) {
                                                return;
                                            }

                                            final var partyMember = PlayerManager.getInstance().fromBukkitPlayer(uuid);
                                            partyMember.sendMessage("Party Chat: " + message);
                                        }
                                    });
                                })))
                .then(new LiteralArgument("list")
                        .executesPlayer((player, _) -> {
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                            networkPlayer.getCachedValue(Party.class).thenAccept(party -> {
                                if (party == null) {
                                    player.sendRichMessage("<red>You are not in a party!");
                                    return;
                                }

                                for (final var uuid : party.getMembers()) {
                                    if (!PlayerManager.getInstance().isRegistered(uuid)) {
                                        return;
                                    }

                                    final var partyMember = PlayerManager.getInstance().fromBukkitPlayer(uuid);
                                    player.sendRichMessage("> " + partyMember.getUsername());
                                }
                            });
                        }))
                .then(new LiteralArgument("create")
                        .executesPlayer((player, _) -> {
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                            PartyManager.getInstance().make(networkPlayer);
                        }))
                .then(new LiteralArgument("disband")
                        .executesPlayer((player, _) -> {
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                            networkPlayer.getCachedValue(Party.class).thenAccept(party -> {
                                if (party == null) {
                                    player.sendRichMessage("<red>You are not in a party!");
                                    return;
                                }

                                if (!party.getOwner().equals(player.getUniqueId())) {
                                    player.sendRichMessage("<red>You are not owner of the party!");
                                    return;
                                }

                                PartyManager.getInstance().disband(party);
                            });
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

                                        if (!party.getOwner().equals(player.getUniqueId())) {
                                            player.sendRichMessage("<red>You are not owner of the party!");
                                            return;
                                        }

                                        if (party.getMembers().contains(target.getUuid())) {
                                            player.sendRichMessage(String.format("<red>%s is already in the party!", target.getUsername()));
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
