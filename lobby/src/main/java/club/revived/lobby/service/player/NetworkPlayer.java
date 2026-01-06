package club.revived.lobby.service.player;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ClusterService;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.exception.ServiceUnavailableException;
import club.revived.lobby.service.exception.UnregisteredPlayerException;
import club.revived.lobby.service.messaging.impl.Connect;
import club.revived.lobby.service.messaging.impl.SendMessage;
import club.revived.lobby.service.status.ServiceStatus;
import club.revived.lobby.service.status.StatusRequest;
import club.revived.lobby.service.status.StatusResponse;
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
     * @return a CompletableFuture that completes with the ClusterService responsible for this player, or `null` if the player is not currently assigned to a proxy
     */
    @NotNull
    public CompletableFuture<ClusterService> whereIs() {
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
     *
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
     * Retrieve a cached value for this player identified by the given class.
     *
     * @param clazz the class used as part of the cache key and to type the returned value
     * @return a CompletableFuture that completes with the cached value for this player and class, or `null` if no value is present
     */
    @NotNull
    public <T> CompletableFuture<T> getCachedValue(final Class<T> clazz) {
        return Cluster.getInstance()
                .getGlobalCache()
                .get(clazz, this.uuid + ":" + clazz.getSimpleName().toLowerCase());
    }

    /**
     * Sends a chat message to this player's current proxy service.
     *
     * @param message the text to send to the player
     * @throws UnregisteredPlayerException if the player's proxy service cannot be located
     */
    public void sendMessage(final String message) {
        this.whereIs().thenAccept(service -> {
            System.out.println("Sending chat message to " + this.username);

            if (service == null) {
                throw new UnregisteredPlayerException("service player is on is not registered");
            }

            service.sendMessage(new SendMessage(this.uuid, message));
        });
    }

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

    public @NotNull UUID getUuid() {
        return uuid;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public @NotNull String getCurrentServer() {
        return currentServer;
    }

    public @NotNull CompletableFuture<ClusterService> getCurrentProxy() {
        return currentProxy;
    }
}