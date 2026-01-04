package club.revived.duels.game.arena.impl;

import club.revived.duels.game.arena.ArenaType;
import club.revived.duels.game.arena.IArena;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * DuelBiome
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class DuelBiome implements IArena {
    /**
     * Gets the first corner of the arena's bounding region.
     *
     * @return the Location representing the first corner of the arena's bounding region
     */
    @Override
    public @NotNull Location getCorner1() {
        return null;
    }

    /**
     * Gets the first player spawn location for this arena.
     *
     * @return the spawn {@link Location} for player one; never {@code null}
     */
    @Override
    public @NotNull Location getSpawn1() {
        return null;
    }

    /**
     * Gets the second corner of the arena's bounding region.
     *
     * @return the {@link Location} representing the arena's second corner; never {@code null}
     */
    @Override
    public @NotNull Location getCorner2() {
        return null;
    }

    /**
     * Gets the second spawn location within this arena.
     *
     * @return the spawn Location for the second player or team
     */
    @Override
    public @NotNull Location getSpawn2() {
        return null;
    }

    /**
     * Returns the unique identifier for this arena.
     *
     * @return the arena identifier string
     */
    @Override
    public @NotNull String getId() {
        return "";
    }

    /**
     * Generate the arena structure at the specified origin location.
     *
     * @param location the origin {@link org.bukkit.Location} where the arena should be generated; must not be null
     */
    @NotNull
    public void generate(final Location location) {

    }

    /**
     * Provides the ArenaType that identifies this arena implementation.
     *
     * @return the ArenaType corresponding to this arena
     */
    @Override
    public @NotNull ArenaType getArenaType() {
        return null;
    }
}