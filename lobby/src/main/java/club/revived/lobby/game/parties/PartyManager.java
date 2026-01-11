package club.revived.lobby.game.parties;

import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.player.NetworkPlayer;

import java.util.ArrayList;
import java.util.Collections;

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
    }

    public void make(final NetworkPlayer player) {
        final var party = new Party(
                player.getUuid(),
                new ArrayList<>(Collections.singleton(player.getUuid())),
                new ArrayList<>(),
                false
        );

        player.sendMessage("<green>Successfully created a new party");
        player.cacheValue(Party.class, party);
    }

    public void acceptRequest(final NetworkPlayer networkPlayer) {
        networkPlayer.getCachedValue(PartyRequest.class).thenAccept(partyRequest -> {
            if (partyRequest == null)  {
                networkPlayer.sendMessage("<red>You don't have any open requests!");
                return;
            }

            final var party = partyRequest.party();

            party.addMember(networkPlayer.getUuid());
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
