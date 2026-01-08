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

    public GameQueue() {
        for (final KitType kit : KitType.values()) {
            queued.put(kit, new LinkedList<>());
        }

        this.registerMessageHandlers();
        this.startTask();
    }

    private void registerMessageHandlers() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(QueuePlayer.class, queuePlayer -> {
                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(queuePlayer.uuid());
                    this.addPlayer(queuePlayer.kitType(), networkPlayer);
                });
    }

    private void addPlayer(KitType kitType, NetworkPlayer player) {
        queued.get(kitType).add(player);
    }

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
