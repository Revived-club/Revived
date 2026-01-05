package club.revived.proxy.service.cluster;

import club.revived.proxy.service.messaging.Message;
import club.revived.proxy.service.messaging.Request;
import club.revived.proxy.service.messaging.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class ClusterService {

    private static final Logger log = LoggerFactory.getLogger(ClusterService.class);

    @NotNull
    private final String id;

    @NotNull
    private final String ip;

    @NotNull
    private final ServiceType type;

    @NotNull
    private final List<OnlinePlayer> onlinePlayers;

    private final long lastSeen;

    /**
     * Creates a ClusterService representing a cluster node with identity, network address, type, current online players, and last-seen timestamp.
     *
     * @param id           unique identifier of the cluster service
     * @param ip           IP address of the cluster service
     * @param type         type/category of the service
     * @param onlinePlayers list of players currently connected to this service
     * @param lastSeen     timestamp (milliseconds since epoch) when the service was last observed active
     */
    public ClusterService(
            final @NotNull String id,
            final @NotNull String ip,
            final @NotNull ServiceType type,
            final @NotNull List<OnlinePlayer> onlinePlayers,
            final long lastSeen
    ) {
        this.id = id;
        this.ip = ip;
        this.type = type;
        this.onlinePlayers = onlinePlayers;
        this.lastSeen = lastSeen;
    }


    /**
     * Send a typed request to this cluster service and obtain the eventual response.
     *
     * @param request      the request payload to send to this service
     * @param responseType the expected response class used to deserialize the reply
     * @param <T>          the response type
     * @return             a CompletableFuture that completes with the response of type `T` when received
     */
    @NotNull
    public <T extends Response> CompletableFuture<T> sendRequest(
            final Request request,
            final Class<T> responseType
    ) {
        return Cluster.getInstance().getMessagingService().sendRequest(
                this.id,
                request,
                responseType
        );
    }

    /**
     * Sends the given message to this cluster service instance.
     *
     * @param message the message to deliver to the cluster service
     */
    public void sendMessage(final Message message) {
        Cluster.getInstance().getMessagingService().sendMessage(this.id, message);
    }

    /**
     * Handles a message delivered to this cluster service.
     *
     * @param message the message to handle; this implementation performs no action
     */
    public <T> void on(final T message) {

    }

    /**
     * The unique identifier of this cluster service.
     *
     * @return the unique identifier of this cluster service, never null
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Retrieves the IP address assigned to this cluster service.
     *
     * @return the IP address of this cluster service
     */
    public @NotNull String getIp() {
        return ip;
    }

    /**
     * Gets the service's type.
     *
     * @return the service's {@link ServiceType}, never null
     */
    public @NotNull ServiceType getType() {
        return type;
    }

    /**
     * Retrieve the list of currently connected players for this service.
     *
     * @return the list of currently connected players, never null
     */
    public @NotNull List<OnlinePlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    /**
     * Retrieve the timestamp when this service was last observed active.
     *
     * @return the timestamp of the last observed activity
     */
    public long getLastSeen() {
        return lastSeen;
    }


}