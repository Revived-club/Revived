package club.revived.duels.service.heartbeat;

import club.revived.duels.service.broker.MessageBroker;
import club.revived.duels.service.broker.MessageHandler;
import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.cluster.ClusterService;
import club.revived.duels.service.cluster.OnlinePlayer;
import club.revived.duels.service.player.PlayerManager;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

    /**
     * Creates a HeartbeatService connected to the given MessageBroker and begins heartbeat lifecycle management.
     *
     * Upon construction, the service subscribes to the "service:heartbeat" topic and schedules the periodic
     * heartbeat publication and timeout-checking task.
     *
     * @param broker the MessageBroker used to subscribe for and publish heartbeat messages
     */
    public HeartbeatService(final MessageBroker broker) {
        this.broker = broker;
        broker.subscribe("service:heartbeat", Heartbeat.class, this);

        this.startTask();
    }

    /**
     * Schedules a delayed task that publishes this service's heartbeat and checks for timed-out cluster services.
     *
     * The scheduled task runs after INTERVAL milliseconds, publishes a Heartbeat message on "service:heartbeat"
     * containing the current timestamp, service type and id, online player count, and cluster IP, and then
     * iterates known services' last-seen timestamps to log an error for any service whose elapsed time exceeds TIMEOUT.
     */
    public void startTask() {
        subServer.scheduleAtFixedRate(() -> {
            broker.publish("service:heartbeat", new Heartbeat(
                    System.currentTimeMillis(),
                    cluster.getServiceType(),
                    cluster.getServiceId(),
                    Bukkit.getOnlinePlayers().size(),
                    Bukkit.getOnlinePlayers().stream()
                            .map(player -> new OnlinePlayer(
                                    player.getUniqueId(),
                                    player.getName(),
                                    this.cluster.getServiceId()
                            ))
                            .toList(),
                    cluster.getIp()
            ));

            for (final String server : lastSeen.keySet()) {
                final var timestamp = lastSeen.get(server);
                final var time = System.currentTimeMillis() - timestamp;

                if (time < TIMEOUT) {
                    log.error("{} timed out after {}ms", server, time);
                }
            }
        }, 0, INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Process an incoming Heartbeat message and reconcile cluster and player state.
     *
     * Updates the last-seen timestamp and the Cluster service entry for the sender, registers each player reported
     * as online in the message with the PlayerManager, and removes any network players whose current server is not
     * present among the message's online players.
     *
     * @param message the Heartbeat payload containing the sender service id, timestamp, service type, server IP and list of online players
     */
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