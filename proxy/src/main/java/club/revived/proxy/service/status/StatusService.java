package club.revived.proxy.service.status;

import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.messaging.MessagingService;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class StatusService {

    /**
     * Creates a StatusService and registers a handler for StatusRequest messages.
     * <p></p>
     * The registered handler responds to each StatusRequest with a StatusResponse
     * containing the current Cluster.STATUS value.
     *
     * @param messagingService the messaging service used to register the request handler
     */
    public StatusService(final MessagingService messagingService) {
        System.out.println( "Starting status service...");
        messagingService.registerHandler(StatusRequest.class, statusRequest -> {
            return new StatusResponse(Cluster.STATUS);
        });
        System.out.println( "Started status service...");
    }
}