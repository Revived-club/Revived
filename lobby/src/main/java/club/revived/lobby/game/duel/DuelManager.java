package club.revived.lobby.game.duel;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.exception.ServiceUnavailableException;
import club.revived.lobby.service.messaging.impl.DuelStart;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import club.revived.lobby.service.status.ServiceStatus;
import club.revived.lobby.service.status.StatusRequest;
import club.revived.lobby.service.status.StatusResponse;

import java.util.List;

/**
 * Handles duels-related things on lobby servers e.g. accepting & sending duel requests.
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class DuelManager {

    private static DuelManager instance;

    /**
     * Initializes a DuelManager and registers it as the singleton instance returned by getInstance().
     */
    public DuelManager() {
        instance = this;
    }

    /**
     * Accepts a pending duel request for the given player and initiates the duel on an available duel service.
     * <p></p>
     * If the player has no pending request, the player is notified and no further action is taken. If the resolved
     * duel service reports a status other than AVAILABLE, the player is notified and a {@link ServiceUnavailableException}
     * is thrown.
     *
     * @param networkPlayer the player attempting to accept a duel request
     * @throws ServiceUnavailableException if the selected duel service reports a non-available status
     */
    public void acceptDuelRequest(final NetworkPlayer networkPlayer) {
        networkPlayer.getCachedValue(DuelRequest.class).thenAccept(duelRequest -> {
            if (duelRequest == null) {
                networkPlayer.sendMessage("<red>You don't have any open requests!");
                return;
            }

            final var service = Cluster.getInstance().getLeastLoadedService(ServiceType.DUEL);

            service.sendRequest(new StatusRequest(), StatusResponse.class).thenAccept(statusResponse -> {
                if (statusResponse.status() != ServiceStatus.AVAILABLE) {
                    networkPlayer.sendMessage("<red>Service is not available");
                    throw new ServiceUnavailableException("requested service is not available");
                }

                networkPlayer.sendMessage("<green>Starting duel...");

                final var sender = PlayerManager.getInstance().fromBukkitPlayer(duelRequest.sender());
                sender.sendMessage("<green>Starting duel...");

                service.sendMessage(new DuelStart(
                        List.of(duelRequest.receiver()),
                        List.of(duelRequest.sender()),
                        duelRequest.rounds(),
                        duelRequest.kitType()
                ));
            });

        });

    }

    /**
     * Store a duel request for the receiver and notify them with a clickable, hoverable acceptance message.
     *
     * <p>The request is saved in the receiver's per-player cache for 120 seconds. The notification includes a clickable
     * action to accept the duel and hover text showing the configured rounds and kit.
     *
     * @param sender   the player who initiated the duel request
     * @param receiver the player who will receive the duel request
     * @param rounds   the number of rounds configured for the duel
     * @param kitType  the kit to be used for the duel
     */
    public void requestDuel(
            final NetworkPlayer sender,
            final NetworkPlayer receiver,
            final int rounds,
            final KitType kitType
    ) {
        receiver.sendMessage("<#3B82F6><player> <white>sent you a duel request! Accept the request by <click:run_command:'/duel accept'><hover:show_text:'<#3B82F6>Click to Accept'><#3B82F6>[Clicking Here]</hover></click>. You can view the duel settings by <hover:show_text:'<#3B82F6>Rounds: <rounds>\n<#3B82F6>Kit: <kit>'><#3B82F6>[Hovering Here]</hover>."
                .replace("<player>", sender.getUsername())
                .replace("<rounds>", String.valueOf(rounds))
                .replace("<kit>", kitType.getBeautifiedName())
        );

        final var request = new DuelRequest(
                sender.getUuid(),
                receiver.getUuid(),
                rounds,
                kitType
        );

        receiver.cacheExValue(
                DuelRequest.class,
                request,
                120
        );
    }

    /**
     * Retrieve the singleton DuelManager instance.
     *
     * @return the initialized DuelManager instance
     * @throws UnsupportedOperationException if the manager has not been initialized
     */
    public static DuelManager getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("DuelManager is not initiated");
        }

        return instance;
    }
}