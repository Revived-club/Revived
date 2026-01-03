package club.revived.lobby.game.duel;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.exception.ServiceUnavailableException;
import club.revived.lobby.service.message.DuelStart;
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

    public DuelManager() {
        instance = this;
    }

    public void acceptDuelRequest(final NetworkPlayer networkPlayer) {
        final var request = Cluster.getInstance().getGlobalCache().get(
                DuelRequest.class,
                "duelRequest:" + networkPlayer.getUuid()
        );

        request.thenAccept(duelRequest -> {
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

    public void requestDuel(
            final NetworkPlayer sender,
            final NetworkPlayer receiver,
            final int rounds,
            final KitType kitType
    ) {
        final var key = "duelRequest:" + receiver.getUuid();

        Cluster.getInstance().getGlobalCache().setEx(key, new DuelRequest(
                sender.getUuid(),
                receiver.getUuid(),
                rounds,
                kitType
        ), 120L);

        receiver.sendMessage("<#3B82F6><player> <white>sent you a duel request! Accept the request by <click:run_command:'/duel accept'><hover:show_text:'<#3B82F6>Click to Accept'><#3B82F6>[Clicking Here]</hover></click>. You can view the duel settings by <hover:show_text:'<#3B82F6>Rounds: <rounds>\n<#3B82F6>Kit: <kit>'><#3B82F6>[Hovering Here]</hover>."
                .replace("<player>", sender.getUsername())
                .replace("<rounds>", String.valueOf(rounds))
                .replace("<kit>", kitType.getBeautifiedName())
        );
    }

    public static DuelManager getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("DuelManager is not initiated");
        }

        return instance;
    }
}
