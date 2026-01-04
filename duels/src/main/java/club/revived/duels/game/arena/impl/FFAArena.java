package club.revived.duels.game.arena.impl;

import club.revived.duels.game.arena.ArenaType;
import club.revived.duels.game.arena.IArena;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * FFAArena
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class FFAArena implements IArena {
    /**
     * Gets the first corner position of this arena.
     *
     * @return the first corner Location of the arena; never null
     */
    @Override
    public @NotNull Location getCorner1() {
        return null;
    }

    /**
     * Provides the primary spawn location for the first spawn point in this arena.
     *
     * @return the first spawn {@link Location}; never {@code null}
     */
    @Override
    public @NotNull Location getSpawn1() {
        return null;
    }

    /**
     * Gets the second corner of the arena's region.
     *
     * @return the location representing the arena's second corner
     */
    @Override
    public @NotNull Location getCorner2() {
        return null;
    }

    /**
     * Provides the second spawn location for this arena.
     *
     * @return the second spawn Location for the arena.
     */
    @Override
    public @NotNull Location getSpawn2() {
        return null;
    }

    /**
     * Get the unique identifier for this arena.
     *
     * @return the arena's unique identifier (never null)
     */
    @Override
    public @NotNull String getId() {
        return "";
    }

    /**
     * Generates this arena at the specified origin location.
     *
     * @param location the origin location where the arena should be created
     */
    @Override
    public void generate(final Location location) {

    }

    /**
     * Identifies the specific ArenaType represented by this arena implementation.
     *
     * @return the ArenaType for this arena
     */
    @Override
    public @NotNull ArenaType getArenaType() {
        return null;
    }
}