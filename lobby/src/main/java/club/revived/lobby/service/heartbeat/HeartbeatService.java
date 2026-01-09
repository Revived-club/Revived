package club.revived.lobby.service.heartbeat;

import club.revived.lobby.service.broker.MessageBroker;
import club.revived.lobby.service.broker.MessageHandler;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ClusterService;
import club.revived.lobby.service.cluster.OnlinePlayer;
import club.revived.lobby.service.player.PlayerManager;
import club.revived.lobby.util.SkinUtils;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final long INTERVAL = 5_000;
    private static final long TIMEOUT = 15_000;
    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);

    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
    private final ScheduledExecutorService subServer = Executors.newScheduledThreadPool(1);
    private final MessageBroker broker;

    private final Cluster cluster = Cluster.getInstance();

    public HeartbeatService(final MessageBroker broker) {
        this.broker = broker;
        broker.subscribe("service:heartbeat", Heartbeat.class, this);

        this.startTask();
    }

    /**
     * Schedules a fixed-rate background task that publishes this service's heartbeat and purges stale services.
     *
     * The task publishes a Heartbeat message containing timestamp, service type/id, online player count and details, and cluster IP,
     * then removes any services from the cluster whose last-seen timestamp exceeds TIMEOUT.
     */
    public void startTask() {
        subServer.scheduleAtFixedRate(() -> {
            try {
                broker.publish("service:heartbeat", new Heartbeat(
                        System.currentTimeMillis(),
                        cluster.getServiceType(),
                        cluster.getServiceId(),
                        Bukkit.getOnlinePlayers().size(),
                        Bukkit.getOnlinePlayers().stream()
                                .map(player -> new OnlinePlayer(
                                        player.getUniqueId(),
                                        player.getName(),
                                        this.cluster.getServiceId(),
                                        player.getPing(),
                                        SkinUtils.getSkin(player),
                                        SkinUtils.getSignature(player)
                                ))
                                .toList(),
                        cluster.getIp()
                ));

                final long now = System.currentTimeMillis();
                for (final String server : lastSeen.keySet()) {
                    final var timestamp = lastSeen.get(server);
                    final var time = now - timestamp;

                    if (time > TIMEOUT) {
                        lastSeen.remove(server);
                        Cluster.getInstance().getServices().remove(server);
                    }
                }
            } catch (Exception e) {
                log.error("Error in heartbeat task", e);
            }
        }, 0, INTERVAL, TimeUnit.MILLISECONDS);
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

        final var serviceId = message.id();

        final var onlinePlayers = message.onlinePlayers();

        PlayerManager.getInstance().getNetworkPlayers()
                .entrySet()
                .removeIf(entry -> {
                    final var networkPlayer = entry.getValue();

                    if (!networkPlayer.getCurrentServer().equalsIgnoreCase(serviceId)) {
                        return false;
                    }

                    return onlinePlayers.stream()
                            .noneMatch(p -> p.uuid().equals(networkPlayer.getUuid()));
                });
    }
}