package club.revived.proxy.service.heartbeat;

import club.revived.proxy.ProxyPlugin;
import club.revived.proxy.service.broker.MessageBroker;
import club.revived.proxy.service.broker.MessageHandler;
import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.cluster.ClusterService;
import club.revived.proxy.service.player.PlayerManager;
import club.revived.proxy.service.status.ServiceStatus;
import club.revived.proxy.service.status.StatusRequest;
import club.revived.proxy.service.status.StatusResponse;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class HeartbeatService implements MessageHandler<Heartbeat> {

    private static final long INTERVAL = 1_000;

    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
    private final ScheduledExecutorService subServer = Executors.newScheduledThreadPool(1);

    private final Cluster cluster = Cluster.getInstance();

    public HeartbeatService(final MessageBroker broker) {
        broker.subscribe("service:heartbeat", Heartbeat.class, this);

        this.startTask();
    }

    public void startTask() {
        subServer.schedule(() -> {
            final var services = this.cluster.getServices()
                    .values()
                    .stream()
                    .toList();

            services.forEach(service -> {
                this.cluster.getMessagingService().sendRequest(
                                service.getId(),
                                new StatusRequest(),
                                StatusResponse.class
                        )
                        .thenAccept(statusResponse -> {
                            if (statusResponse.status() != ServiceStatus.AVAILABLE) {
                                return;
                            }

                            final var str = service.getIp().split(":");
                            final var host = str[0];
                            final var port = Integer.parseInt(str[1]);

                            final var info = new ServerInfo(service.getId(), new InetSocketAddress(host, port));

                            ProxyPlugin.getInstance()
                                    .getServer()
                                    .getServer(service.getId())
                                    .ifPresent(server -> {
                                        final var serverInfo = server.getServerInfo();

                                        if (!serverInfo.equals(info)) {
                                            this.registerServer(info);
                                        }
                                    });

                            this.registerServer(info);
                        });
            });

        }, INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void registerServer(final ServerInfo serverInfo) {
        ProxyPlugin.getInstance()
                .getServer()
                .registerServer(serverInfo);
    }

    @Override
    public void handle(final Heartbeat message) {
        final var service = new ClusterService(
                message.id(),
                message.serverIp(),
                message.serviceType(),
                message.onlinePlayers(),
                message.timestamp()
        );

        this.lastSeen.put(
                message.id(),
                message.timestamp()
        );

        Cluster.getInstance().getServices().put(
                message.id(),
                service
        );

        message.onlinePlayers().forEach(onlinePlayer -> PlayerManager.getInstance().registerPlayer(
                onlinePlayer.uuid(),
                onlinePlayer.username(),
                onlinePlayer.currentServer()
        ));

        PlayerManager.getInstance().getNetworkPlayers()
                .entrySet()
                .removeIf(entry ->
                        message.onlinePlayers().stream().noneMatch(p -> {
                            final var uuid = p.uuid();
                            final var value = entry.getValue();
                            final var entryUUID = value.getUuid();

                            final var valueService = value.getCurrentServer();
                            final var playerService = p.currentServer();

                            return uuid.equals(entryUUID) && playerService.equalsIgnoreCase(valueService);
                        }));
    }
}
