package club.revived.queue;

import club.revived.queue.cluster.cluster.Cluster;
import club.revived.queue.cluster.cluster.ServiceType;
import club.revived.queue.cluster.messaging.impl.DuelStart;
import club.revived.queue.cluster.status.ServiceStatus;
import club.revived.queue.cluster.status.StatusRequest;
import club.revived.queue.cluster.status.StatusResponse;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;

/**
 * GameQueue - matches players for duels and sends them to sub-servers.
 *
 * @author yyuh
 * @since 1/8/26
 */
public final class GameQueue implements IQueue<UUID, QueueEntry> {

    private final Map<KitType, Map<QueueType, Deque<QueueEntry>>> queue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public GameQueue() {
        for (final KitType kit : KitType.values()) {
            final Map<QueueType, Deque<QueueEntry>> map = new EnumMap<>(QueueType.class);

            for (final QueueType type : QueueType.values()) {
                map.put(type, new ConcurrentLinkedDeque<>());
            }

            queue.put(kit, map);
        }
    }


    @Override
    public void startTask() {
        executorService.scheduleAtFixedRate(() -> {

            for (final KitType kit : KitType.values()) {
                for (final QueueType type : QueueType.values()) {

                    final Deque<QueueEntry> queued =
                            queue.get(kit).get(type);

                    final int required = type.totalPlayers();

                    while (queued.size() >= required) {
                        final List<QueueEntry> entries = new ArrayList<>(required);

                        for (int i = 0; i < required; i++) {
                            entries.add(queued.removeFirst());
                        }

                        pop(entries.toArray(new QueueEntry[0]));
                    }
                }
            }

        }, 0, 1, TimeUnit.SECONDS);
    }


    @Override
    public void push(final QueueEntry entry) {
        queue.get(entry.kitType())
                .get(entry.queueType())
                .add(entry);
    }

    @Override
    public void pop(final QueueEntry... entries) {
        final QueueType type = entries[0].queueType();

        final List<QueueEntry> blue = Arrays.stream(entries)
                .limit(type.teamSize())
                .toList();

        final List<QueueEntry> red = Arrays.stream(entries)
                .skip(type.teamSize())
                .limit(type.teamSize())
                .toList();

        final DuelStart duelStart = new DuelStart(
                blue.stream().map(QueueEntry::uuid).toList(),
                red.stream().map(QueueEntry::uuid).toList(),
                type.teamSize(),
                entries[0].kitType()
        );

        final var service = Cluster.getInstance()
                .getLeastLoadedService(ServiceType.DUEL);

        service.sendRequest(new StatusRequest(), StatusResponse.class)
                .thenAccept(status -> {
                    if (status.status() == ServiceStatus.AVAILABLE) {
                        service.sendMessage(duelStart);
                    }
                });
    }


    @Override
    public void remove(final UUID uuid) {
        this.queue.forEach((_, queueEntries) ->
                queueEntries.values().forEach(entries ->
                        entries.removeIf(queueEntry -> queueEntry.uuid().equals(uuid))));
    }

    @Override
    public @NotNull List<QueueEntry> queued() {
        return List.of();
    }
}