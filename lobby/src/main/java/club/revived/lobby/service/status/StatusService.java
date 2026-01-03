package club.revived.lobby.service.status;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.messaging.MessagingService;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class StatusService {

    public StatusService(final MessagingService messagingService) {
        messagingService.registerHandler(StatusRequest.class, statusRequest -> new StatusResponse(Cluster.STATUS));
    }
}
