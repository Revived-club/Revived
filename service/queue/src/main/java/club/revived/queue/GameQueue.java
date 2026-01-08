package club.revived.queue;

import club.revived.queue.cluster.cluster.Cluster;
import club.revived.queue.cluster.cluster.ServiceType;
import club.revived.queue.cluster.messaging.impl.AddToQueue;
import club.revived.queue.cluster.messaging.impl.DuelStart;
import club.revived.queue.cluster.player.PlayerManager;
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

    /**
     * Initializes the GameQueue and populates its internal data structure for all kit and queue types.
     *
     * <p>For each KitType, creates an EnumMap that maps every QueueType to a new ConcurrentLinkedDeque to hold QueueEntry instances.
     */
    public GameQueue() {
        for (final KitType kit : KitType.values()) {
            final Map<QueueType, Deque<QueueEntry>> map = new EnumMap<>(QueueType.class);

            for (final QueueType type : QueueType.values()) {
                map.put(type, new ConcurrentLinkedDeque<>());
            }

            queue.put(kit, map);
        }

        this.registerMessageHandlers();
        this.startTask();
    }

    /**
     * Registers message handlers for the queue
     */
    private void registerMessageHandlers() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(AddToQueue.class, addToQueue -> {
                    System.out.printf("Adding %s to the queue!%n", addToQueue.uuid().toString());

                    final var queueEntry = new QueueEntry(
                            addToQueue.uuid(),
                            addToQueue.queueType(),
                            addToQueue.kitType()
                    );

                    this.push(queueEntry);
                });
    }

    /**
     * Starts the recurring task that processes all kit and queue-type queues.
     * <p>
     * Every second it notifies players who are currently present in a queue with an action
     * bar message and, when a queue has at least the required number of entries for a
     * QueueType, removes that many entries and dispatches them to be matched via {@code pop(...)}.
     */
    @Override
    public void startTask() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                for (final KitType kit : KitType.values()) {
                    for (final QueueType type : QueueType.values()) {

                        final Deque<QueueEntry> queued =
                                queue.get(kit).get(type);

                        for (final var entry : queued) {
                            if (!PlayerManager.getInstance().getNetworkPlayers().containsKey(entry.uuid())) {
                                System.out.println("not in the list");
                                queued.remove(entry);
                                continue;
                            }

                            System.out.println("Actionbar");
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(entry.uuid());
                            networkPlayer.sendActionbar("<red>You are in queue...");
                        }

                        final int required = type.totalPlayers();

                        final List<QueueEntry> entries = new ArrayList<>(required);

                        for (int i = 0; i < required; i++) {
                            final QueueEntry entry = queued.pollFirst();

                            System.out.println("hit");

                            if (entry == null) {
                                entries.forEach(queued::addFirst);
                                return;
                            }

                            entries.add(entry);
                        }

                        pop(entries.toArray(new QueueEntry[0]));

                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    /**
     * Enqueues the given entry into the queue corresponding to its kit and queue type.
     *
     * @param entry the QueueEntry that identifies the player, kit, and queue type to add
     */
    @Override
    public void push(final QueueEntry entry) {
        queue.get(entry.kitType())
                .get(entry.queueType())
                .add(entry);
    }

    /**
     * Creates a DuelStart from the provided queue entries and submits it to an available duel service.
     * <p>
     * The first entry determines the queue type and kit. The method groups the entries into two teams:
     * the first `teamSize` entries as the blue team and the next `teamSize` entries as the red team,
     * builds a DuelStart with their UUIDs and the kit, and sends it to a duel service if one reports
     * AVAILABLE status.
     *
     * @param entries an array of queue entries representing players to form the match; must contain
     *                exactly `2 * teamSize` entries where `teamSize` is taken from `entries[0].queueType()`
     */
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


    /**
     * Removes any queue entries with the given player UUID from all kit and queue-type queues.
     *
     * @param uuid the player's UUID whose entries should be removed
     */
    @Override
    public void remove(final UUID uuid) {
        this.queue.forEach((_, queueEntries) ->
                queueEntries.values().forEach(entries ->
                        entries.removeIf(queueEntry -> queueEntry.uuid().equals(uuid))));
    }

    /**
     * Retrieve the current queued entries across all kits and queue types (currently always empty).
     *
     * @return a list containing all queued `QueueEntry` objects; currently an empty list
     */
    @Override
    public @NotNull List<QueueEntry> queued() {
        return List.of();
    }
}