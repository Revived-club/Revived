package club.revived.proxy.service.cluster;

import club.revived.proxy.ProxyPlugin;
import club.revived.proxy.service.broker.MessageBroker;
import club.revived.proxy.service.cache.GlobalCache;
import club.revived.proxy.service.heartbeat.HeartbeatService;
import club.revived.proxy.service.messaging.MessagingService;
import club.revived.proxy.service.player.impl.WhereIsProxyRequest;
import club.revived.proxy.service.player.impl.WhereIsProxyResponse;
import club.revived.proxy.service.player.impl.WhereIsRequest;
import club.revived.proxy.service.player.impl.WhereIsResponse;
import club.revived.proxy.service.status.ServiceStatus;
import club.revived.proxy.service.status.StatusService;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Comparator;
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

    private final ProxyServer proxyServer = ProxyPlugin.getInstance().getServer();

    public Cluster(
            final @NotNull MessageBroker broker,
            final @NotNull GlobalCache cache,
            final @NotNull String id
    ) {
        this(broker, cache, ServiceType.UNASSIGNED, id);
    }

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

        startServices();
        registerRequestHandlers();
    }

    private void startServices() {
        new HeartbeatService(this.broker);
        new StatusService(this.messagingService);
    }

    private void registerRequestHandlers() {
        this.messagingService.registerHandler(WhereIsProxyRequest.class, whereIsProxyRequest -> {
            final var player = this.proxyServer.getPlayer(whereIsProxyRequest.uuid()).orElse(null);

            if (player == null) {
                return null;
            }

            return new WhereIsProxyResponse(this.serviceId);
        });
    }

    public <T> void send(
            final String id,
            final T message
    ) {
        this.broker.publish(id, message);
    }

    @NotNull
    public ClusterService getLeastLoadedService(final ServiceType serviceType) {
        final var services = this.services.values()
                .stream()
                .filter(clusterService -> clusterService.getType() == serviceType)
                .sorted(Comparator.comparingInt(service -> service.getOnlinePlayers().size()))
                .toList();

        return services.getFirst();
    }

    @NotNull
    public CompletableFuture<ClusterService> whereIsProxy(final UUID uuid) {
        return this.messagingService.sendRequest("global", new WhereIsProxyRequest(uuid), WhereIsProxyResponse.class)
                .thenApply(whereIsResponse -> {
                    final var id = whereIsResponse.proxy();

                    return this.services.get(id);
                });
    }

    @NotNull
    public CompletableFuture<ClusterService> whereIs(final UUID uuid) {
        return this.messagingService.sendRequest("global", new WhereIsRequest(uuid), WhereIsResponse.class)
                .thenApply(whereIsResponse -> {
                    final var id = whereIsResponse.server();

                    return this.services.get(id);
                });
    }

    @NotNull
    private String serviceIp() {
        try {
            final var ip = InetAddress.getLocalHost().getHostAddress();
            final var port = 19132;

            return ip + ":" + port;
        } catch (final Exception e) {
            throw new IllegalStateException("Service failed to get IP");
        }
    }

    public @NotNull String getIp() {
        return ip;
    }

    public @NotNull MessageBroker getBroker() {
        return broker;
    }

    public @NotNull GlobalCache getGlobalCache() {
        return globalCache;
    }

    public @NotNull MessagingService getMessagingService() {
        return messagingService;
    }

    public @NotNull Map<String, ClusterService> getServices() {
        return services;
    }

    public @NotNull ServiceType getServiceType() {
        return serviceType;
    }

    public @NotNull String getServiceId() {
        return serviceId;
    }

    public static Cluster getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("There is no cluster registered!");
        }

        return instance;
    }
}
