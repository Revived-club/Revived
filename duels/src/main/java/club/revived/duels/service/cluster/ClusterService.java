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

    public void sendMessage(final Message message) {
        Cluster.getInstance().getMessagingService().sendMessage(this.id, message);
    }

    public <T> void on(final T message) {

    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull String getIp() {
        return ip;
    }

    public @NotNull ServiceType getType() {
        return type;
    }

    public @NotNull List<OnlinePlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public long getLastSeen() {
        return lastSeen;
    }


}
