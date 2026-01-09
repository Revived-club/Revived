package club.revived.lobby.service.player;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ClusterService;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.exception.ServiceUnavailableException;
import club.revived.lobby.service.exception.UnregisteredPlayerException;
import club.revived.lobby.service.messaging.impl.Connect;
import club.revived.lobby.service.messaging.impl.SendActionbar;
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

    @NotNull
    public CompletableFuture<ClusterService> whereIsProxy() {
        return Cluster.getInstance().whereIsProxy(this.uuid);
    }


    /**
     * Determines the cluster service category responsible for this player.
     *
     * @return the `ServiceType` of the cluster service that currently handles this player
     */
    @NotNull
    public CompletableFuture<ServiceType> getService() {
        return this.whereIs().thenApply(ClusterService::getType);
    }


    /**
     * Store an object in the cluster-wide global cache for this player with a time-to-live.
     *
     * The value is stored under the key "<playerUuid>:<clazzSimpleNameLowercased>".
     *
     * @param clazz   the class whose simple name (lowercased) is used as the cache key suffix
     * @param obj     the object to store in the global cache for this player
     * @param seconds time-to-live for the cached value, in seconds
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
     * Store an object in the cluster-wide global cache for this player.
     *
     * The value is stored under the key "{playerUuid}:{clazzSimpleNameLowercased}".
     *
     * @param clazz the class whose simple name (lowercased) is appended to the player's UUID to form the cache key
     * @param obj   the object to store
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
         * Retrieves the cached value for this player for the specified class.
         *
         * @param clazz the class used as part of the cache key and to type the returned value
         * @return the cached value for this player and class, or `null` if no value is present
         */
    @NotNull
    public <T> CompletableFuture<T> getCachedValue(final Class<T> clazz) {
        return Cluster.getInstance()
                .getGlobalCache()
                .get(clazz, this.uuid + ":" + clazz.getSimpleName().toLowerCase());
    }

    /**
     * Sends a chat message to the player's current proxy service.
     *
     * @param message the message text to deliver to the player
     * @throws UnregisteredPlayerException if no proxy service is registered for this player
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

    /**
     * Sends an action bar message to the player's current proxy service.
     *
     * @param message the message to display in the action bar
     * @throws UnregisteredPlayerException if the player is not associated with a proxy service
     */
    public void sendActionbar(final String message) {
        this.whereIs().thenAccept(service -> {
            System.out.println("Sending chat message to " + this.username);

            if (service == null) {
                throw new UnregisteredPlayerException("service player is on is not registered");
            }

            service.sendMessage(new SendActionbar(this.uuid, message));
        });
    }

    /**
     * Initiates a connection request for this player to the specified cluster service through the player's proxy.
     *
     * Sends a StatusRequest to the target service and, if the service reports AVAILABLE, forwards a Connect payload
     * containing this player's UUID and the target service ID to the player's current proxy.
     *
     * @param clusterService the target cluster service to connect the player to
     * @throws ServiceUnavailableException if the target service reports a status other than AVAILABLE
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