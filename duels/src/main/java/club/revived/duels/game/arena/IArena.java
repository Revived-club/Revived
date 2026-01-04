package club.revived.duels.game.arena;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * IArena
 *
 * @author yyuh
 * @since 04.01.26
 */
public interface IArena {

    /**
     * Provide the first corner of the arena's bounding region.
     *
     * @return the Location of the arena's first corner; never {@code null}
     */
    @NotNull
    Location getCorner1();

    /**
     * Gets the first spawn location for this arena.
     *
     * @return the first player spawn {@link Location} inside the arena; never {@code null}
     */
    @NotNull
    Location getSpawn1();

    /**
     * The second corner of the arena's bounding region.
     *
     * @return the second corner {@link Location} defining the arena bounds; never {@code null}
     */
    @NotNull
    Location getCorner2();

    /**
     * Gets the second spawn location for players in this arena.
     *
     * @return the arena's second spawn Location
     */
    @NotNull
    Location getSpawn2();

    /**
     * Gets the arena's unique identifier.
     *
     * @return the arena identifier string (never {@code null})
     */
    @NotNull
    String getId();

    /**
 * Generate and place this arena at the specified location.
 *
 * @param location the world location that serves as the anchor or reference point for generation
 */
void generate(final Location location);

    /**
     * Gets the arena's type.
     *
     * @return the ArenaType of this arena
     */
    @NotNull
    ArenaType getArenaType();

}