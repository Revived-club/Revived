package club.revived.proxy.service.status;

import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.messaging.MessagingService;
import club.revived.proxy.service.status.StatusRequest;
import club.revived.proxy.service.status.StatusResponse;

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
