package club.revived.queue.cluster.cluster;

import club.revived.queue.cluster.broker.MessageBroker;
import club.revived.queue.cluster.cache.GlobalCache;
import club.revived.queue.cluster.heartbeat.HeartbeatService;
import club.revived.queue.cluster.messaging.MessagingService;
import club.revived.queue.cluster.messaging.impl.*;
import club.revived.queue.cluster.player.PlayerManager;
import club.revived.queue.cluster.status.ServiceStatus;
import club.revived.queue.cluster.status.StatusRequest;
import club.revived.queue.cluster.status.StatusResponse;
import club.revived.queue.cluster.status.StatusService;
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
     * Creates and initializes a Cluster for the given service identity, wiring messaging,
     * heartbeat/status subsystems and registering request handlers.
     * <p>
     * This constructor sets the singleton instance for the process, computes the service IP,
     * instantiates the MessagingService, starts background services, and registers request handlers.
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
        new PlayerManager();
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
                BotDuelStart.class,
                Connect.class,
                DuelStart.class,
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
     * Register handlers for cluster request messages.
     *
     * <p>Registers a handler for WhereIsRequest that responds with a WhereIsResponse containing this
     * service's ID when the requested player is online, or a WhereIsResponse with a null service ID
     * when the player is not found.
     */
    private void registerRequestHandlers() {

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
     * Locate the ClusterService acting as the proxy for the given player UUID.
     *
     * @return the ClusterService hosting the player's proxy, or {@code null} if no proxy is known for the UUID
     */
    @NotNull
    public CompletableFuture<ClusterService> whereIsProxy(final UUID uuid) {
        return this.messagingService.sendRequest("global", new WhereIsProxyRequest(uuid), WhereIsProxyResponse.class)
                .thenApply(whereIsResponse -> {
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
     * Returns the local service address in the form "ip:port".
     *
     * @return the local IP address and port (e.g. "192.168.1.2:3000")
     * @throws IllegalStateException if the local host address cannot be determined
     */
    @NotNull
    private String serviceIp() {
        try {
            final var ip = InetAddress.getLocalHost().getHostAddress();
            final var port = 3000;

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
     * Retrieve the unique identifier for this service instance.
     *
     * @return the service instance's unique identifier
     */
    public @NotNull String getServiceId() {
        return serviceId;
    }

    /**
     * Retrieve the process-wide Cluster singleton.
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