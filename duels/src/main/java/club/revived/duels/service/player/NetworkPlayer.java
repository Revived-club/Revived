package club.revived.duels.service.player;

import club.revived.duels.database.DatabaseManager;
import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.cluster.ClusterService;
import club.revived.duels.service.cluster.ServiceType;
import club.revived.duels.service.exception.ServiceUnavailableException;
import club.revived.duels.service.exception.UnregisteredPlayerException;
import club.revived.duels.service.messaging.impl.Connect;
import club.revived.duels.service.messaging.impl.SendMessage;
import club.revived.duels.service.status.ServiceStatus;
import club.revived.duels.service.status.StatusRequest;
import club.revived.duels.service.status.StatusResponse;
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
     * Creates a NetworkPlayer for the given identity and current server and initiates resolution of the player's proxy service.
     *
     * Initializes the player's UUID, username, and current server, and begins asynchronous lookup of the proxy service responsible for this player (stored in {@code currentProxy}).
     *
     * @param uuid the player's unique identifier
     * @param username the player's display name
     * @param currentServer the identifier of the server the player is currently on
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
     * Locate the cluster proxy service responsible for this player.
     *
     * @return the ClusterService responsible for this player, or null if the player is not currently assigned to a proxy
     */
    @NotNull
    private CompletableFuture<ClusterService> whereIs() {
        return Cluster.getInstance().whereIs(this.uuid);
    }

    /**
         * Determine which kind of cluster service currently handles the player.
         *
         * @return the ServiceType of the cluster service that handles the player
         */
    @NotNull
    public CompletableFuture<ServiceType> getService() {
        return this.whereIs().thenApply(ClusterService::getType);
    }

    /**
     * Cache the given object for this player in the cluster-wide global cache with an expiration.
     *
     * The entry is stored under the key "{playerUuid}:{clazzSimpleNameLowercased}".
     *
     * @param clazz   class whose simple name (lowercased) is used as the cache key suffix
     * @param obj     the object to store for this player
     * @param seconds expiration time in seconds for the cached entry
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
     * Caches an object for this player in the cluster-wide global cache.
     *
     * The value is stored under the key "<playerUuid>:<clazzSimpleNameLowercased>".
     *
     * @param clazz the class whose simple name (lowercased) is used as the cache key suffix
     * @param obj   the object to store for this player
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
     * Retrieves the cached value for this player associated with the given class.
     *
     * @param clazz the class whose simple name is used in the per-player cache key and that determines the returned value's type
     * @return the cached value for this player and class, or {@code null} if no value is present
     */
    @NotNull
    public <T> CompletableFuture<T> getCachedValue(final Class<T> clazz) {
        return Cluster.getInstance()
                .getGlobalCache()
                .get(clazz, this.uuid + ":" + clazz.getSimpleName().toLowerCase());
    }

    /**
     * Retrieves a cached value for this player by type or loads it from the database and caches it if absent.
     *
     * @param <T>   the type of the value
     * @param clazz the class used to identify and load the value
     * @return the cached or database-loaded instance for this player, or `null` if not found
     */
    @NotNull
    public <T> CompletableFuture<T> getCachedOrLoad(final Class<T> clazz) {
        return this.getCachedValue(clazz).thenCompose(t -> {
            if (t != null) {
                return CompletableFuture.completedFuture(t);
            }

            return DatabaseManager.getInstance().get(clazz, this.uuid.toString())
                    .thenApply(opt -> {
                        final T val = opt.orElse(null);
                        if (val != null) {
                            this.cacheValue(clazz, val);
                        }
                        return val;
                    });
        });
    }

    /**
     * Sends a chat message to this player's current proxy service.
     *
     * @param message the text to send to the player
     * @throws UnregisteredPlayerException if the player's proxy service cannot be located
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
     * Requests a connection to the specified cluster service and instructs the player's current proxy to
     * perform the connection if the target service reports an AVAILABLE status.
     *
     * @param clusterService the target cluster service to connect the player to
     * @throws ServiceUnavailableException if the target service's status is not AVAILABLE
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
     * Initiates a connection sequence to the cluster service with the given identifier.
     *
     * Sends a status request to the target service and, if the service reports AVAILABLE,
     * directs the player's current proxy to send a Connect message containing this player's UUID
     * and the target service id.
     *
     * @param id the identifier of the target cluster service
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
     * Gets the player's unique identifier.
     *
     * @return the player's UUID
     */
    public @NotNull UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the player's username.
     *
     * @return the player's username, never null
     */
    public @NotNull String getUsername() {
        return username;
    }

    /**
     * Identifier of the server the player is currently on.
     *
     * @return the identifier of the server the player is currently on
     */
    public @NotNull String getCurrentServer() {
        return currentServer;
    }

    /**
     * Obtain the proxy service currently handling this player.
     *
     * @return a CompletableFuture that completes with the proxy ClusterService for this player, or `null` if no proxy is assigned
     */
    public @NotNull CompletableFuture<ClusterService> getCurrentProxy() {
        return currentProxy;
    }
}