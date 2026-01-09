package club.revived.duels.game.arena.pooling;

import club.revived.duels.game.arena.ArenaCreator;
import club.revived.duels.game.arena.ArenaType;
import club.revived.duels.game.arena.IArena;
import club.revived.duels.game.duels.KitType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ArenaPoolManager {

    private static final int ARENA_POOL_SIZE = 3;

    private static ArenaPoolManager instance;

    private final Map<ArenaType, Deque<IArena>> arenaPool = new ConcurrentHashMap<>();

    /**
     * Initializes a new ArenaPoolManager and registers it as the singleton instance.
     */
    private ArenaPoolManager() {
        instance = this;
    }

    /**
     * Provides access to the singleton ArenaPoolManager instance.
     *
     * @return the singleton ArenaPoolManager instance; creates and returns a new instance if one does not yet exist
     */
    public static ArenaPoolManager getInstance() {
        if (instance == null) {
            return new ArenaPoolManager();
        }
        return instance;
    }

    /**
     * Initialize and seed the arena pools for every ArenaType.
     *
     * Ensures a deque exists for each ArenaType and fills each pool up to the configured pool size.
     *
     * @return a CompletableFuture that completes when all arena pools have been created and seeded
     */
    @NotNull
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            for (final ArenaType arenaType : ArenaType.values()) {
                this.arenaPool.putIfAbsent(arenaType, new ArrayDeque<>());

                this.refillArenaPool(arenaType);
            }
        });
    }

    /**
     * Obtains an arena matching the given kit's arena type, using a pooled instance when available.
     *
     * If a pooled arena is returned, the pool is asynchronously replenished with a newly generated arena.
     *
     * @param kitType the kit whose associated ArenaType will be used to select or create an arena
     * @return the arena instance for the specified kit's arena type
     */
    @NotNull
    public CompletableFuture<IArena> getArena(final @NotNull KitType kitType) {
        final ArenaType arenaType = kitType.getArenaType();
        final var arenas = arenaPool.computeIfAbsent(arenaType, _ -> new ArrayDeque<>());

        final IArena arena = arenas.pollFirst();

        if (arena != null) {
            generateArena(arenaType).thenAccept(arenas::addLast);
            return CompletableFuture.completedFuture(arena);
        }

        return generateArena(arenaType);
    }

    /**
     * Refills the pool for the specified arena type until it contains ARENA_POOL_SIZE arenas.
     *
     * Generates missing arenas asynchronously and appends each created arena to the end of the pool's deque.
     *
     * @param arenaType the arena type whose pool should be replenished
     */
    private void refillArenaPool(final ArenaType arenaType) {
        final var arenas = arenaPool.get(arenaType);
        final int missing = ARENA_POOL_SIZE - arenas.size();

        for (int i = 0; i < missing; i++) {
            this.generateArena(arenaType).thenAccept(arenas::addLast);
        }
    }

    /**
     * Creates a new arena of the specified arena type.
     *
     * @param arenaType the arena type to create
     * @return the created IArena
     */
    @NotNull
    private CompletableFuture<IArena> generateArena(final @NotNull ArenaType arenaType) {
        return ArenaCreator.getInstance().makeOf(arenaType);
    }
}