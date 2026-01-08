package club.revived.queue;

import club.revived.queue.cluster.cluster.Cluster;
import club.revived.queue.cluster.cluster.ServiceType;
import club.revived.queue.cluster.messaging.impl.DuelStart;
import club.revived.queue.cluster.messaging.impl.QueuePlayer;
import club.revived.queue.cluster.player.NetworkPlayer;
import club.revived.queue.cluster.player.PlayerManager;
import club.revived.queue.cluster.status.ServiceStatus;
import club.revived.queue.cluster.status.StatusRequest;
import club.revived.queue.cluster.status.StatusResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * GameQueue - matches players for duels and sends them to sub-servers.
 *
 * @author yyuh
 * @since 1/8/26
 */
public final class GameQueue {

    private final Map<KitType, LinkedList<NetworkPlayer>> queued = new ConcurrentHashMap<>();
    private final ScheduledExecutorService subServer = Executors.newScheduledThreadPool(1);

    /**
     * Initializes per-kit player queues, registers message handlers, and starts the periodic matchmaking task.
     *
     * Populates the internal map with an empty LinkedList for every KitType, sets up message handling for queue
     * updates, and begins the scheduled task that pairs players and dispatches duels.
     */
    public GameQueue() {
        for (final KitType kit : KitType.values()) {
            queued.put(kit, new LinkedList<>());
        }

        this.registerMessageHandlers();
        this.startTask();
    }

    /**
     * Registers messaging handlers required by the queue system.
     *
     * Specifically, sets up a handler for `QueuePlayer` messages that converts the sender's UUID
     * to a `NetworkPlayer` and adds that player to the appropriate kit queue.
     */
    private void registerMessageHandlers() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(QueuePlayer.class, queuePlayer -> {
                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(queuePlayer.uuid());
                    this.addPlayer(queuePlayer.kitType(), networkPlayer);
                });
    }

    /**
     * Enqueues a player into the queue for the specified kit type.
     *
     * @param kitType the kit category whose queue the player will be added to
     * @param player the player to enqueue
     */
    private void addPlayer(KitType kitType, NetworkPlayer player) {
        queued.get(kitType).add(player);
    }

    /**
     * Schedules a recurring task that pairs queued players by kit and initiates duels on available sub-servers.
     *
     * The task runs once per second and, for each kit queue, repeatedly removes pairs of players and attempts to start
     * a duel on a suitable sub-server. If the chosen service is not available, the two players are reinserted at the
     * front of their queue in original order so they can be retried later.
     */
    private void startTask() {
        subServer.scheduleAtFixedRate(() -> {

            for (final KitType kitType : queued.keySet()) {
                final LinkedList<NetworkPlayer> inQueue = queued.get(kitType);

                while (inQueue.size() >= 2) {
                    final NetworkPlayer player1 = inQueue.removeFirst();
                    final NetworkPlayer player2 = inQueue.removeFirst();

                    final var service = Cluster.getInstance().getLeastLoadedService(ServiceType.DUEL);

                    service.sendRequest(new StatusRequest(), StatusResponse.class)
                            .thenAccept(statusResponse -> {
                                if (statusResponse.status() != ServiceStatus.AVAILABLE) {
                                    synchronized (inQueue) {
                                        inQueue.addFirst(player2);
                                        inQueue.addFirst(player1);
                                    }

                                    return;
                                }

                                service.sendMessage(new DuelStart(
                                        List.of(player1.getUuid()),
                                        List.of(player2.getUuid()),
                                        1,
                                        kitType
                                ));
                            });
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}