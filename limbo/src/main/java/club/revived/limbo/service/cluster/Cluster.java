package club.revived.limbo.service.cluster;

import club.revived.limbo.service.broker.MessageBroker;
import club.revived.limbo.service.cache.GlobalCache;
import club.revived.limbo.service.heartbeat.HeartbeatService;
import club.revived.limbo.service.messaging.MessagingService;
import club.revived.limbo.service.messaging.impl.*;
import club.revived.limbo.service.status.ServiceStatus;
import club.revived.limbo.service.status.StatusRequest;
import club.revived.limbo.service.status.StatusResponse;
import club.revived.limbo.service.status.StatusService;
import com.loohp.limbo.Limbo;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Cluster {

    private static Cluster instance;

    @NotNull
    private final String serviceId;

    @NotNull
    private final ServiceType serviceType;

    @NotNull
    private final MessageBroker broker;

    @NotNull
    private final GlobalCache globalCache;

    @NotNull
    private final MessagingService messagingService;

    @NotNull
    private final Map<String, ClusterService> services = new ConcurrentHashMap<>();

    @NotNull
    private final String ip;

    public static ServiceStatus STATUS = ServiceStatus.STARTING;

    /**
     * Creates a Cluster configured with the provided messaging broker and global cache and registers it
     * under the given service id with a service type of {@code ServiceType.UNASSIGNED}.
     *
     * @param broker the message broker used for inter-service communication
     * @param cache  the shared global cache instance
     * @param id     the unique identifier for this service instance
     */
    public Cluster(
            final @NotNull MessageBroker broker,
            final @NotNull GlobalCache cache,
            final @NotNull String id
    ) {
        this(broker, cache, ServiceType.UNASSIGNED, id);
    }

    /**
     * Initialize a Cluster for the given service identity and wire messaging, heartbeat, and
     * discovery subsystems for this process instance.
     *
     * @param broker      the MessageBroker used for inter-service publish/subscribe
     * @param cache       the shared GlobalCache instance
     * @param serviceType the type of this service
     * @param id          the unique identifier for this service instance
     * @throws IllegalStateException if the local service IP cannot be determined
     */
    public Cluster(
            final @NotNull MessageBroker broker,
            final @NotNull GlobalCache cache,
            final @NotNull ServiceType serviceType,
            final @NotNull String id
    ) {
        this.broker = broker;
        this.serviceType = serviceType;
        this.serviceId = id;
        this.ip = this.serviceIp();
        this.messagingService = new MessagingService(broker, id);
        this.globalCache = cache;

        instance = this;

        this.startServices();
        this.registerRequestHandlers();
        this.registerMessageTypes();
    }

    /**
     * Initializes and starts the cluster background services required for operation.
     * <p>
     * Starts the heartbeat service and the status service so the cluster can broadcast liveness
     * and report status to other components.
     */
    private void startServices() {
        new HeartbeatService(this.broker);
        new StatusService(this.messagingService);
    }

    /**
     * Registers all message classes used by the cluster with the messaging service.
     *
     * <p>Ensures the messaging service is aware of the cluster's request/response and event
     * message types so they can be serialized and dispatched across the cluster.</p>
     */
    private void registerMessageTypes() {
        List.of(
                Connect.class,
                SendMessage.class,
                WhereIsProxyResponse.class,
                WhereIsProxyRequest.class,
                WhereIsRequest.class,
                WhereIsResponse.class,
                StatusRequest.class,
                StatusResponse.class
        ).forEach(this.messagingService::register);
    }

    /**
     * Registers messaging handlers for cluster requests.
     *
     * <p>Registers a handler for WhereIsRequest that returns a WhereIsResponse containing this
     * service's ID when the requested player is currently online, or `null` when the player is not found.
     */
    private void registerRequestHandlers() {
        this.messagingService.registerHandler(WhereIsRequest.class, whereIsRequest -> {

            final var player = Limbo.getInstance().getPlayer(whereIsRequest.uuid());

            if (player == null) {
                return null;
            }

            return new WhereIsResponse(this.serviceId);
        });
    }

    /**
     * Publishes the given message to the cluster message broker using the specified destination id.
     *
     * @param id      the destination id or routing key to publish the message to
     * @param message the message payload to be published
     */
    public <T> void send(
            final String id,
            final T message
    ) {
        this.broker.publish(id, message);
    }

    /**
     * Selects the least-loaded ClusterService for the given service type.
     *
     * @param serviceType the type of service to search for
     * @return the ClusterService of the specified type with the fewest online players
     * @throws java.util.NoSuchElementException if no service of the specified type is registered
     */
    @NotNull
    public ClusterService getLeastLoadedService(final ServiceType serviceType) {
        final var services = this.services.values()
                .stream()
                .filter(clusterService -> clusterService.getType() == serviceType)
                .sorted(Comparator.comparingInt(service -> service.getOnlinePlayers().size()))
                .toList();

        return services.getFirst();
    }

    /**
     * Resolve which ClusterService is acting as the proxy for the given player UUID.
     *
     * @param uuid the player's unique identifier to locate
     * @return the ClusterService hosting the player's proxy, or `null` if no proxy is known for the UUID
     */
    @NotNull
    public CompletableFuture<ClusterService> whereIsProxy(final UUID uuid) {
        return this.messagingService.sendGlobalRequest(new WhereIsProxyRequest(uuid), WhereIsProxyResponse.class)
                .thenApply(whereIsResponses -> {
                    final var whereIsResponse = whereIsResponses.getFirst();

                    final var id = whereIsResponse.proxy();

                    return this.services.get(id);
                });
    }

    /**
     * Locate the cluster service currently hosting the player with the given UUID.
     *
     * @param uuid the player's UUID to locate
     * @return the ClusterService hosting the player with the given UUID, or `null` if unknown
     */
    @NotNull
    public CompletableFuture<ClusterService> whereIs(final UUID uuid) {
        return this.messagingService.sendGlobalRequest(new WhereIsRequest(uuid), WhereIsResponse.class)
                .thenApply(whereIsResponse -> {
                    final var id = whereIsResponse.getFirst().server();

                    return this.services.get(id);
                });
    }

    /**
     * Constructs the local service address string in the form "ip:port".
     *
     * @return the local IP address concatenated with the Bukkit port, e.g. "192.168.1.2:25565"
     * @throws IllegalStateException if the local host address or Bukkit port cannot be determined
     */
    @NotNull
    private String serviceIp() {
        try {
            final var ip = InetAddress.getLocalHost().getHostAddress();
            final var port = 30000;

            return ip + ":" + port;
        } catch (final Exception e) {
            throw new IllegalStateException("Service failed to get IP");
        }
    }

    /**
     * Gets the computed IP and port string for this service.
     *
     * @return the service's IP and port in the form "address:port"
     */
    public @NotNull String getIp() {
        return ip;
    }

    /**
     * Provides the message broker used for cluster inter-service messaging.
     *
     * @return the MessageBroker instance used for publish/subscribe communication within the cluster
     */
    public @NotNull MessageBroker getBroker() {
        return broker;
    }

    /**
     * Provides access to the shared global cache used by the cluster.
     *
     * @return the cluster's GlobalCache instance
     */
    public @NotNull GlobalCache getGlobalCache() {
        return globalCache;
    }

    /**
     * Provides access to the cluster's messaging service.
     *
     * @return the MessagingService used for inter-service messaging (never {@code null})
     */
    public @NotNull MessagingService getMessagingService() {
        return messagingService;
    }

    /**
     * Accesses the map of known cluster services keyed by service ID.
     *
     * @return the map of known `ClusterService` instances keyed by service ID
     */
    public @NotNull Map<String, ClusterService> getServices() {
        return services;
    }

    /**
     * Obtains the service type for this Cluster.
     *
     * @return the ServiceType identifying this service instance
     */
    public @NotNull ServiceType getServiceType() {
        return serviceType;
    }

    /**
     * Returns the unique identifier for this service instance.
     *
     * @return the service instance's unique identifier
     */
    public @NotNull String getServiceId() {
        return serviceId;
    }

    /**
     * Get the registered Cluster singleton instance.
     *
     * @return the registered Cluster instance
     * @throws UnsupportedOperationException if no Cluster instance has been registered
     */
    public static Cluster getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("There is no cluster registered!");
        }

        return instance;
    }
}