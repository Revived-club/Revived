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

    @Override
    public void generate(final Location location) {
        log.info("Pasting schematic for arena {}", this.id);
        SchematicPaster.paste(this.schematic, this.corner1);
    }

    @Override
    public @NotNull Location getCorner1() {
        return this.corner1;
    }

    @Override
    public @NotNull Location getSpawn1() {
        return this.spawn1;
    }

    @Override
    public @NotNull Location getCorner2() {
        return this.corner2;
    }

    @Override
    public @NotNull Location getSpawn2() {
        return this.spawn2;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull ArenaType getArenaType() {
        return this.arenaType;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }
}
