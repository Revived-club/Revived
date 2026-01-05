package club.revived.lobby.game.duel;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.exception.ServiceUnavailableException;
import club.revived.lobby.service.messaging.impl.DuelStart;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.status.ServiceStatus;
import club.revived.lobby.service.status.StatusRequest;
import club.revived.lobby.service.status.StatusResponse;

import java.util.List;

/**
 * DuelManager
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
     * Attempts to accept a pending duel request for the given player and, if a duel service is available, initiates the duel.
     *
     * If no pending request exists the player is notified. If the resolved duel service reports a non-available status,
     * the player is notified and a {@link ServiceUnavailableException} is thrown.
     *
     * @param networkPlayer the player attempting to accept a duel request
     * @throws ServiceUnavailableException if the selected duel service is not available
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
     * Stores a duel request for the receiver and notifies them with a clickable, hoverable acceptance message.
     *
     * The request is saved in the global cache under the key "duelRequest:&lt;receiver UUID&gt;" with a 120-second expiry.
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

        final var key = "duelRequest:" + receiver.getUuid();

        Cluster.getInstance().getGlobalCache().setEx(key, new DuelRequest(
                sender.getUuid(),
                receiver.getUuid(),
                rounds,
                kitType
        ), 120L);
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