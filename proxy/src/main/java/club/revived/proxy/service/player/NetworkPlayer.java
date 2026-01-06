package club.revived.proxy.service.player;

import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.cluster.ClusterService;
import club.revived.proxy.service.cluster.ServiceType;
import club.revived.proxy.service.exception.ServiceUnavailableException;
import club.revived.proxy.service.exception.UnregisteredPlayerException;
import club.revived.proxy.service.messaging.impl.Connect;
import club.revived.proxy.service.messaging.impl.SendMessage;
import club.revived.proxy.service.status.ServiceStatus;
import club.revived.proxy.service.status.StatusRequest;
import club.revived.proxy.service.status.StatusResponse;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class NetworkPlayer {

    @NotNull
    private final UUID uuid;

    @NotNull
    private final String username;

    @NotNull
    private final String currentServer;

    @NotNull
    private final CompletableFuture<ClusterService> currentProxy;

    /**
     * Create a NetworkPlayer and initialize its proxy lookup.
     * <p>
     * Initializes the player's identity, current server, and starts an asynchronous lookup
     * for the proxy service responsible for this player's UUID.
     *
     * @param uuid          the player's unique identifier
     * @param username      the player's username
     * @param currentServer the name of the server the player is currently on
     */
    public NetworkPlayer(
            final @NotNull UUID uuid,
            final @NotNull String username,
            final @NotNull String currentServer
    ) {
        this.uuid = uuid;
        this.username = username;
        this.currentServer = currentServer;
        this.currentProxy = Cluster.getInstance().whereIsProxy(this.uuid);
    }

    /**
     * Locates the cluster proxy service responsible for this player.
     *
     * @return the {@link ClusterService} responsible for this player, or {@code null} if the player is not currently assigned to a proxy
     */
    @NotNull
    private CompletableFuture<ClusterService> whereIs() {
        return Cluster.getInstance().whereIs(this.uuid);
    }

    /**
     * Obtains the type of the cluster service currently responsible for this player.
     *
     * @return the player's current proxy service type
     */
    @NotNull
    public CompletableFuture<ServiceType> getService() {
        return this.whereIs().thenApply(ClusterService::getType);
    }

    /**
     * Cache an object for this player in the cluster-wide global cache.
     * <p>
     * The value is stored under the key "{playerUuid}:{clazzSimpleNameLowercased}".
     *
     * @param clazz the class whose simple name (lowercased) is used as part of the cache key
     * @param obj   the object to store in the global cache for this player
     */
    public <T> void cacheValue(
            final Class<T> clazz,
            final T obj
    ) {
        Cluster.getInstance()
                .getGlobalCache()
                .set(this.uuid + ":" + clazz.getSimpleName().toLowerCase(), obj);
    }

    /**
     * Store an object in the cluster-wide global cache for this player with a time-to-live.
     *
     * The value is stored under the key "{playerUuid}:{clazzSimpleNameLowercased}".
     *
     * @param clazz   the class whose simple name (lowercased) is used as the key suffix
     * @param obj     the object to store in the global cache for this player
     * @param seconds the time-to-live for the cached entry, in seconds
     */
    public <T> void cacheExValue(
            final Class<T> clazz,
            final T obj,
            final long seconds
    ) {
        Cluster.getInstance()
                .getGlobalCache()
                .setEx(
                        this.uuid + ":" + clazz.getSimpleName().toLowerCase(),
                        obj,
                        seconds
                );
    }

    /**
     * Retrieve the cached value associated with this player for the provided class.
     *
     * @param clazz the class used to form the cache key and to type the retrieved value
     * @return the cached value for this player and class, or {@code null} if no value is present
     */
    @NotNull
    public <T> CompletableFuture<T> getCachedValue(final Class<T> clazz) {
        return Cluster.getInstance()
                .getGlobalCache()
                .get(clazz, this.uuid + ":" + clazz.getSimpleName().toLowerCase());
    }

    /**
     * Send a chat message to the proxy currently handling this player.
     *
     * @param message the text to deliver to the player
     * @throws UnregisteredPlayerException if no proxy service is registered for this player
     */
    public void sendMessage(
            final String message
    ) {
        final var whereIs = this.whereIs();

        whereIs.thenAccept(service -> {
            if (service == null) {
                throw new UnregisteredPlayerException("service player is on is not registered");
            }

            service.sendMessage(new SendMessage(this.uuid, message));
        });
    }

    /**
     * Requests connection of this player to the specified cluster service.
     * <p>
     * Sends a status check to the target service and, if the service reports `ServiceStatus.AVAILABLE`,
     * instructs the player's current proxy to initiate the connection by sending a `Connect` message
     * containing this player's UUID and the target service ID.
     *
     * @param clusterService the target service to connect the player to
     * @throws ServiceUnavailableException if the target service reports a status other than `ServiceStatus.AVAILABLE`
     */
    public void connect(final ClusterService clusterService) {
        clusterService.sendRequest(new StatusRequest(), StatusResponse.class)
                .thenAccept(statusResponse -> {
                    if (statusResponse.status() != ServiceStatus.AVAILABLE) {
                        throw new ServiceUnavailableException("Trying to connect to a service that's not available");
                    }

                    this.currentProxy.thenAccept(service -> {
                        service.sendMessage(new Connect(
                                this.uuid,
                                clusterService.getId()
                        ));
                    });

                });
    }

    /**
     * Requests a transfer of this player to the service identified by the given id.
     * <p>
     * Sends a status check to the target service and, if its status is AVAILABLE, instructs the player's current proxy to connect the player to that service.
     *
     * @param id the identifier of the target service
     * @throws ServiceUnavailableException if the target service reports a status other than AVAILABLE
     */
    public void connect(final String id) {
        final var clusterService = Cluster.getInstance()
                .getServices()
                .get(id);

        clusterService.sendRequest(new StatusRequest(), StatusResponse.class)
                .thenAccept(statusResponse -> {
                    if (statusResponse.status() != ServiceStatus.AVAILABLE) {
                        throw new ServiceUnavailableException("Trying to connect to a service that's not available");
                    }

                    this.currentProxy.thenAccept(service -> {
                        service.sendMessage(new Connect(
                                this.uuid,
                                clusterService.getId()
                        ));
                    });
                });
    }

    /**
     * Retrieves the player's unique identifier.
     *
     * @return the player's UUID (never null)
     */
    public @NotNull UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the player's username.
     *
     * @return the player's username
     */
    public @NotNull String getUsername() {
        return username;
    }

    /**
     * The name of the server the player is currently on.
     *
     * @return the current server name
     */
    public @NotNull String getCurrentServer() {
        return currentServer;
    }

    /**
     * The ClusterService currently known to be the player's proxy.
     *
     * @return the ClusterService currently acting as this player's proxy
     */
    public @NotNull CompletableFuture<ClusterService> getCurrentProxy() {
        return currentProxy;
    }
}