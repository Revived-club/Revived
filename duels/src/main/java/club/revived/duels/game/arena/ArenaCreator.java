package club.revived.duels.game.arena;

import club.revived.duels.game.arena.impl.DuelArena;
import club.revived.duels.game.arena.schematic.SchematicProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ArenaCreator
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class ArenaCreator {

    private static ArenaCreator instance;

    private int currentX = 0;
    private int currentZ = 0;
    private int stepCount = 0;
    private int stepLimit = 1;
    private int turnCount = 0;
    private int direction = 0;

    /**
     * Creates a new ArenaCreator and registers it as the singleton instance.
     */
    public ArenaCreator() {
        instance = this;
    }

    /**
     * Create an arena instance for the given arena type using a randomly selected schematic and place it at the next generated location.
     *
     * @param arenaType the type of arena to create
     * @return a CompletableFuture that completes with the created {@link IArena}, or completes exceptionally if no schematics are available for the specified arena type
     */
    @NotNull
    public CompletableFuture<IArena> makeOf(final ArenaType arenaType) {
        final var schematics = SchematicProvider.getInstance()
                .getSchematics()
                .get(arenaType);

        if (schematics.isEmpty()) {
            return CompletableFuture.failedFuture(new RuntimeException("there are no schematics for this arena type"));
        }

        return CompletableFuture.supplyAsync(() -> {
            final var random = ThreadLocalRandom.current();
            final var schematic = schematics.get(random.nextInt(schematics.size()));
            final var location = this.getNextLocation();

            final double dx = location.getX() - schematic.corner1().getX();
            final double dy = location.getY() - schematic.corner1().getY();
            final double dz = location.getZ() - schematic.corner1().getZ();

            final Location corner1 = schematic.corner1().clone();
            final Location corner2 = schematic.corner2().clone();
            final Location spawn1 = schematic.spawn1().clone();
            final Location spawn2 = schematic.spawn2().clone();

            corner1.setWorld(location.getWorld());
            corner2.setWorld(location.getWorld());
            spawn1.setWorld(location.getWorld());
            spawn2.setWorld(location.getWorld());

            corner1.add(dx, dy, dz);
            corner2.add(dx, dy, dz);
            spawn1.add(dx, dy, dz);
            spawn2.add(dx, dy, dz);

            final var worldEditSchematic = SchematicProvider.getInstance()
                    .getWorldeditSchematics()
                    .get(schematic.id());

            final var arena = new DuelArena(
                    schematic.corner1(),
                    schematic.corner2(),
                    schematic.arenaType(),
                    worldEditSchematic.file()
            );

            arena.setSpawn1(schematic.spawn1());
            arena.setSpawn2(schematic.spawn2());

            arena.generate(location);

            return arena;
        });

    }

    /**
     * Compute the next arena placement location on the arena grid.
     *
     * Produces a Location in the "duels" world at coordinates (currentX * 1000, 100, currentZ * 1000)
     * corresponding to the current grid position before advancing. Advances the internal grid cursor
     * along an outward spiral pattern and updates currentX, currentZ, direction, stepCount, stepLimit,
     * and turnCount to prepare for the following call.
     *
     * @return the computed Location for the next arena placement
     */
    @NotNull
    public Location getNextLocation() {
        final Location loc = new Location(
                Bukkit.getWorld("world"),
                currentX * 1000,
                100,
                currentZ * 1000
        );


        switch (direction) {
            case 0 -> currentX++;
            case 1 -> currentZ++;
            case 2 -> currentX--;
            case 3 -> currentZ--;
        }

        stepCount++;
        if (stepCount >= stepLimit) {
            stepCount = 0;
            direction = (direction + 1) % 4;
            turnCount++;
            if (turnCount % 2 == 0) {
                stepLimit++;
            }
        }

        return loc;
    }

    /**
     * Retrieves the singleton ArenaCreator, creating and registering a new instance if none exists.
     *
     * @return the singleton ArenaCreator instance
     */
    public static ArenaCreator getInstance() {
        if (instance == null) {
            return new ArenaCreator();
        }

        return instance;
    }
}