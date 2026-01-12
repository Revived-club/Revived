package club.revived.lobby.game.parties;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.messaging.impl.QuitNetwork;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * PartyManager
 *
 * @author yyuh
 * @since 11.01.26
 */
public final class PartyManager {

    private static PartyManager instance;

    public PartyManager() {
        instance = this;
        this.initializeMessageHandlers();
    }

    private void initializeMessageHandlers() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(QuitNetwork.class, quitNetwork -> {
                    System.out.println("test " + quitNetwork);

                    Cluster.getInstance().getGlobalCache()
                            .get(
                                    Party.class,
                                    quitNetwork.uuid() + ":" + Party.class.getSimpleName().toLowerCase()
                            )
                            .thenAccept(party -> {
                                if (party == null) {
                                    return;
                                }

                                if (party.getOwner().equals(quitNetwork.uuid())) {
                                    this.changeOwnership(party, quitNetwork.uuid());
                                }

                                Cluster.getInstance().getGlobalCache().set(
                                        quitNetwork.uuid() + ":" + Party.class.getSimpleName().toLowerCase(),
                                        null
                                );
                            });
                });
    }

    public void make(final NetworkPlayer player) {
        player.getCachedValue(Party.class).thenAccept(p -> {
            if (p != null) {
                player.sendMessage("<red>You are already in a party!");
                return;
            }

            final var party = new Party(
                    player.getUuid(),
                    new ArrayList<>(Collections.singleton(player.getUuid())),
                    new ArrayList<>()
            );

            Cluster.getInstance().getGlobalCache().push("parties", party);

            player.sendMessage("<green>Successfully created a new party");
            player.cacheValue(Party.class, party);
        });
    }

    public void disband(final Party party) {
        for (final UUID member : party.getMembers()) {
            final NetworkPlayer networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(member);
            networkPlayer.cacheValue(Party.class, null);
            networkPlayer.sendMessage("The party you are in got disbanded");
        }

        party.setDisbanded(true);
        party.update();
    }

    public void changeOwnership(final Party party, final UUID oldOwner) {
        final var random = ThreadLocalRandom.current();
        final var uuids = party.getMembers();

        uuids.remove(oldOwner);

        final UUID uuid = uuids.get(random.nextInt(uuids.size()));
        party.setOwner(uuid);

        final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(uuid);
        networkPlayer.sendMessage("<green>You are the new owner of the party!");
        party.update();
    }

    public void transferOwnership(final Party party, final UUID newOwner) {
        party.setOwner(newOwner);

        final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(newOwner);
        networkPlayer.sendMessage("<green>You are the new owner of the party!");
        party.update();
    }

    public void kick(final Party party, final UUID uuid) {
        party.getMembers().remove(uuid);
        party.update();

        final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(uuid);
        networkPlayer.cacheValue(Party.class, null);
        networkPlayer.sendMessage("<red>You have been kicked from the party");
    }

    public void acceptRequest(final NetworkPlayer networkPlayer) {
        networkPlayer.getCachedValue(PartyRequest.class).thenAccept(partyRequest -> {
            if (partyRequest == null) {
                networkPlayer.sendMessage("<red>You don't have any open requests!");
                return;
            }

            final var party = partyRequest.party();

            if (party.isDisbanded()) {
                networkPlayer.sendMessage("<red>The party you are trying to join is disbanded!");
                return;
            }

            party.addMember(networkPlayer.getUuid());

            for (final var uuid : party.getMembers()) {
                if (!PlayerManager.getInstance().getNetworkPlayers().containsKey(uuid)) {
                    continue;
                }

                final var member = PlayerManager.getInstance().fromBukkitPlayer(uuid);
                member.sendMessage(String.format("%s joined the party!", networkPlayer.getUsername()));
            }
        });
    }

    public void request(
            final NetworkPlayer sender,
            final NetworkPlayer receiver,
            final Party party
    ) {
        receiver.whereIs().thenAccept(clusterService -> {
            if (clusterService.getType() != ServiceType.LOBBY) {
                sender.sendMessage(String.format("<red>%s is not in a lobby", receiver.getUsername()));
                return;
            }

            receiver.sendMessage("<#3B82F6><player> <white>invited you to their party! Accept the request by <click:run_command:'/party accept'><hover:show_text:'<#3B82F6>Click to Accept'><#3B82F6>[Clicking Here]</hover></click>. <white>You can view the member list <hover:show_text:'<members>'><#3B82F6>[Here]</hover>"
                    .replace("<player>", sender.getUsername())
                    .replace("<members>", String.join("\n", party.players().stream().map(NetworkPlayer::getUsername).toList()))
            );

            final PartyRequest request = new PartyRequest(
                    sender.getUuid(),
                    receiver.getUuid(),
                    party
            );

            receiver.cacheExValue(
                    PartyRequest.class,
                    request,
                    120
            );
        });
    }

    public static PartyManager getInstance() {
        if (instance == null) {
            return new PartyManager();
        }

        return instance;
    }
}
