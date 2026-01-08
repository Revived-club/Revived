package club.revived.queue.cluster.cluster;

import club.revived.queue.cluster.cluster.Cluster;
import club.revived.queue.cluster.cluster.OnlinePlayer;
import club.revived.queue.cluster.cluster.ServiceType;
import club.revived.queue.cluster.messaging.Message;
import club.revived.queue.cluster.messaging.Request;
import club.revived.queue.cluster.messaging.Response;
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
         * Send a request to this cluster service and obtain its response.
         *
         * @param request      the request to send to the remote service
         * @param responseType the expected response class used to deserialize the reply
         * @param <T>          the response type
         * @return the response of type {@code T} from the remote service
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
     * Hook invoked when a message is received from this cluster service.
     *
     * Default implementation is a no-op; subclasses should override to handle specific message types.
     *
     * @param message the incoming message to handle; implementations may inspect and act on its concrete type
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
     * Timestamp (milliseconds since epoch) when the service was last observed.
     *
     * @return the millisecond timestamp since the Unix epoch representing when the service was last seen
     */
    public long getLastSeen() {
        return lastSeen;
    }


}