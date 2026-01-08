package club.revived.duels.game.arena.impl;

import club.revived.commons.generic.StringUtils;
import club.revived.commons.worldedit.SchematicPaster;
import club.revived.duels.game.arena.ArenaType;
import club.revived.duels.game.arena.IArena;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DuelArena
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class DuelArena implements IArena {

    private static final Logger log = LoggerFactory.getLogger(DuelArena.class);
    private final String id;
    private final Location corner1;
    private final Location corner2;
    private final ArenaType arenaType;
    private Location spawn1;
    private Location spawn2;

    private final File schematic;

    private final List<Location> modifiedLocations = new ArrayList<>();

    /**
     * Creates a DuelArena with an auto-generated identifier and the specified corners, arena type, and schematic.
     *
     * @param corner1  first corner of the arena bounding box
     * @param corner2  opposite corner of the arena bounding box
     * @param arenaType arena type for this arena
     * @param schematic schematic file used to paste the arena layout
     */
    public DuelArena(
            final Location corner1,
            final Location corner2,
            final ArenaType arenaType,
            final File schematic
    ) {
        this(
                StringUtils.generateId("#duel-"),
                corner1,
                corner2,
                arenaType,
                schematic
        );
    }

    /**
     * Creates a DuelArena with the specified identifier, bounding corners, arena type, and schematic.
     *
     * @param id         unique identifier for the arena
     * @param corner1    one corner of the arena's bounding region
     * @param corner2    opposite corner of the arena's bounding region
     * @param arenaType  type of the arena
     * @param schematic  schematic file to paste when generating the arena
     */
    public DuelArena(
            final String id,
            final Location corner1,
            final Location corner2,
            final ArenaType arenaType,
            final File schematic
    ) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.id = id;
        this.arenaType = arenaType;
        this.schematic = schematic;
    }

    /**
     * Pastes this arena's schematic into the world at the arena's first corner.
     *
     * The provided location parameter is ignored; the schematic is always pasted at this arena's configured corner1.
     *
     * @param location accepted for API compatibility and not used by this implementation
     */
    @Override
    public void generate(final Location location) {
        log.info("Pasting schematic for arena {}", this.id);
        SchematicPaster.paste(this.schematic, this.corner1);
    }

    /**
     * The first corner of the arena's bounding region.
     *
     * @return the `Location` of the arena's first bounding corner
     */
    @Override
    public @NotNull Location getCorner1() {
        return this.corner1;
    }

    /**
     * Retrieve the first player spawn location for this arena.
     *
     * @return the first player spawn Location
     */
    @Override
    public @NotNull Location getSpawn1() {
        return this.spawn1;
    }

    /**
     * The second corner location that defines the arena's bounding box.
     *
     * @return the arena's second corner Location
     */
    @Override
    public @NotNull Location getCorner2() {
        return this.corner2;
    }

    /**
     * Returns the second player spawn location for this arena.
     *
     * @return the second player spawn location (never `null`)
     */
    @Override
    public @NotNull Location getSpawn2() {
        return this.spawn2;
    }

    /**
     * Gets the arena's unique identifier.
     *
     * @return the arena's unique id
     */
    @Override
    public @NotNull String getId() {
        return this.id;
    }

    /**
     * Retrieves the arena's type.
     *
     * @return the arena's ArenaType
     */
    @Override
    public @NotNull ArenaType getArenaType() {
        return this.arenaType;
    }

    /**
     * Sets the arena's second player spawn location.
     *
     * @param spawn2 the Location to use for the second player's spawn point
     */
    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }

    /**
     * Sets the first player spawn location for this arena.
     *
     * @param spawn1 the location to assign as the first spawn point
     */
    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }

    /**
     * Gets the schematic file used to build this arena.
     *
     * @return the File pointing to the arena's schematic
     */
    public File getSchematic() {
        return schematic;
    }

    /**
     * Provides the list of locations that have been modified in this arena.
     * <p></p>
     * The returned list is the internal, mutable list used to track modified locations.
     *
     * @return the internal mutable List of modified Location objects
     */
    public List<Location> getModifiedLocations() {
        return modifiedLocations;
    }
}