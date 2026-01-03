package club.revived.lobby.service.player;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ClusterService;
import club.revived.lobby.service.exception.ServiceUnavailableException;
import club.revived.lobby.service.exception.UnregisteredPlayerException;
import club.revived.lobby.service.player.impl.Connect;
import club.revived.lobby.service.player.impl.SendMessage;
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

    @NotNull
    private CompletableFuture<ClusterService> whereIs() {
        return Cluster.getInstance().whereIs(this.uuid);
    }

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
