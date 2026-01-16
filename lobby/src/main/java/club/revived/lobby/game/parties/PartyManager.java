package club.revived.lobby.game.parties;

import club.revived.commons.generic.ListUtils;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.exception.ServiceUnavailableException;
import club.revived.lobby.service.messaging.impl.DuelStart;
import club.revived.lobby.service.messaging.impl.FFAStart;
import club.revived.lobby.service.messaging.impl.QuitNetwork;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import club.revived.lobby.service.status.ServiceStatus;
import club.revived.lobby.service.status.StatusRequest;
import club.revived.lobby.service.status.StatusResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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

                                party.removeMember(quitNetwork.uuid());
                                party.save();
                            });
                });
    }

    public CompletableFuture<Party> make(final NetworkPlayer player) {
        return player.getCachedValue(String.class).thenApply(existingPartyId -> {
            if (existingPartyId != null) {
                player.sendMessage("<red>You are already in a party!");
                return null;
            }

            final Party party = new Party(
                    player.getUuid(),
                    new ArrayList<>(List.of(player.getUuid())),
                    new ArrayList<>()
            );

            player.cacheValue(Party.class, party);
            player.sendMessage("<green>Party created");

            return party;
        });
    }


    public void disband(final Party party) {
        final var cache = Cluster.getInstance().getGlobalCache();

        for (UUID member : List.copyOf(party.getMembers())) {
            final NetworkPlayer player = PlayerManager.getInstance().fromBukkitPlayer(member);
            player.cacheValue(Party.class, null);
            player.sendMessage("<red>Your party was disbanded");
        }

        cache.removeFromList("parties", party.getId(), 1);
    }

    public void changeOwnership(
            final Party party,
            final UUID oldOwner
    ) {
        final List<UUID> members = new ArrayList<>(party.getMembers());
        members.remove(oldOwner);

        if (members.isEmpty()) {
            disband(party);
            return;
        }

        final UUID newOwner = members.get(
                ThreadLocalRandom.current().nextInt(members.size())
        );

        party.setOwner(newOwner);
        party.save();

        final NetworkPlayer player = PlayerManager.getInstance().fromBukkitPlayer(newOwner);
        player.sendMessage("<green>You are the new owner of the party!");
    }

    public void transferOwnership(final Party party, final UUID newOwner) {
        if (!party.getMembers().contains(newOwner)) {
            return;
        }

        party.setOwner(newOwner);

        final NetworkPlayer player = PlayerManager.getInstance().fromBukkitPlayer(newOwner);
        player.sendMessage("<green>You are now the party owner");
    }


    public void kick(final Party party, final UUID uuid) {
        if (party.getOwner().equals(uuid)) {
            return;
        }

        if (!party.getMembers().contains(uuid)) {
            return;
        }

        party.getMembers().remove(uuid);
        party.save();

        final var cache = Cluster.getInstance().getGlobalCache();
        cache.remove("player:" + uuid + ":party");

        final NetworkPlayer player = PlayerManager.getInstance().fromBukkitPlayer(uuid);
        player.cacheValue(Party.class, null);
        player.sendMessage("<red>You have been kicked from the party");
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

    public void startFFA(
            final Party party,
            final KitType kitType
    ) {
        final var service = Cluster.getInstance().getLeastLoadedService(ServiceType.DUEL);

        service.sendRequest(new StatusRequest(), StatusResponse.class).thenAccept(statusResponse -> {
            if (statusResponse.status() != ServiceStatus.AVAILABLE) {
                party.broadcast("<red>There has been an error with the service you were trying to connect to!");
                throw new ServiceUnavailableException("requested service is not available");
            }

            final var teams = ListUtils.splitInHalf(party.getMembers());

            party.broadcast("<green>Starting fight...");

            service.sendMessage(new FFAStart(
                    party.getMembers(),
                    kitType
            ));
        });
    }

    public void startGame(
            final Party party,
            final int rounds,
            final KitType kitType
    ) {
        final var service = Cluster.getInstance().getLeastLoadedService(ServiceType.DUEL);

        service.sendRequest(new StatusRequest(), StatusResponse.class).thenAccept(statusResponse -> {
            if (statusResponse.status() != ServiceStatus.AVAILABLE) {
                party.broadcast("<red>There has been an error with the service you were trying to connect to!");
                throw new ServiceUnavailableException("requested service is not available");
            }

            final var teams = ListUtils.splitInHalf(party.getMembers());
            final var redTeam = teams.getFirst();
            final var blueTeam = teams.get(1);

            party.broadcast("<green>Starting fight...");

            service.sendMessage(new DuelStart(
                    blueTeam,
                    redTeam,
                    rounds,
                    kitType
            ));
        });
    }

    public static PartyManager getInstance() {
        if (instance == null) {
            return new PartyManager();
        }

        return instance;
    }
}
