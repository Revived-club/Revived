package club.revived.duels.service.cluster;

import club.revived.duels.service.messaging.Message;
import club.revived.duels.service.messaging.Request;
import club.revived.duels.service.messaging.Response;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class ClusterService {

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
     * @param type         category/type of the service
     * @param onlinePlayers list of currently online players associated with this service
     * @param lastSeen     timestamp (milliseconds since epoch) when the service was last observed
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
     * Sends a request to this cluster service and obtains the corresponding response.
     *
     * @param request      the request to send to the remote service
     * @param responseType the expected response class used to deserialize the reply
     * @param <T>          the response type
     * @return the response of type `T`
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
     * Sends a message to this cluster service.
     *
     * @param message the message payload to deliver to the service
     */
    public void sendMessage(final Message message) {
        Cluster.getInstance().getMessagingService().sendMessage(this.id, message);
    }

    /**
     * Handles an incoming cluster message.
     *
     * Default implementation does nothing; subclasses may override to react to specific message types.
     *
     * @param message the received message to handle
     */
    public <T> void on(final T message) {

    }

    /**
     * Gets the unique identifier for this cluster service.
     *
     * @return the non-null unique identifier of the cluster service
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Retrieves the cluster service's IP address.
     *
     * @return the IP address of this cluster service
     */
    public @NotNull String getIp() {
        return ip;
    }

    /**
     * Service type of this cluster member.
     *
     * @return the service's {@link ServiceType}, never {@code null}
     */
    public @NotNull ServiceType getType() {
        return type;
    }

    /**
     * Provides the online players associated with this cluster service.
     *
     * @return the list of online players for this service; never {@code null}
     */
    public @NotNull List<OnlinePlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    /**
     * Gets the timestamp when the service was last seen.
     *
     * @return the timestamp when the service was last seen
     */
    public long getLastSeen() {
        return lastSeen;
    }


}