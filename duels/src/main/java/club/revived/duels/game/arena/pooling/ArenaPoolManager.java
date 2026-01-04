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

    private ArenaPoolManager() {
        instance = this;
    }

    public static ArenaPoolManager getInstance() {
        if (instance == null) {
            return new ArenaPoolManager();
        }
        return instance;
    }

    @NotNull
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            for (final ArenaType arenaType : ArenaType.values()) {
                this.arenaPool.putIfAbsent(arenaType, new ArrayDeque<>());

                this.refillArenaPool(arenaType);
            }
        });
    }

    @NotNull
    public CompletableFuture<IArena> getArena(final @NotNull KitType kitType) {
        final ArenaType arenaType = kitType.getArenaType();
        final var arenas = arenaPool.computeIfAbsent(arenaType, k -> new ArrayDeque<>());

        final IArena arena = arenas.pollFirst();

        if (arena != null) {
            generateArena(arenaType).thenAccept(arenas::addLast);
            return CompletableFuture.completedFuture(arena);
        }

        return generateArena(arenaType);
    }

    private void refillArenaPool(final ArenaType arenaType) {
        final var arenas = arenaPool.get(arenaType);
        final int missing = ARENA_POOL_SIZE - arenas.size();

        for (int i = 0; i < missing; i++) {
            this.generateArena(arenaType).thenAccept(arenas::addLast);
        }
    }

    @NotNull
    private CompletableFuture<IArena> generateArena(final @NotNull ArenaType arenaType) {
        return ArenaCreator.getInstance().makeOf(arenaType);
    }
}