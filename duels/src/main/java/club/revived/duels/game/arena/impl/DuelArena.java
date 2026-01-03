package club.revived.duels.game.arena.impl;

import club.revived.commons.StringUtils;
import club.revived.duels.game.arena.ArenaType;
import club.revived.duels.game.arena.IArena;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * DuelArena
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class DuelArena implements IArena {

    private final String id;
    private final Location corner1;
    private final Location corner2;
    private Location spawn1;
    private Location spawn2;
    private ArenaType arenaType;

    public DuelArena(
            final Location corner1,
            final Location corner2
    ) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.id = StringUtils.generateId("#duel-");
    }

    public DuelArena(
            final String id,
            final Location corner1,
            final Location corner2
    ) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.id = id;
    }


    @Override
    public @NotNull Location getCorner1() {
        return null;
    }

    @Override
    public @NotNull Location getSpawn1() {
        return null;
    }

    @Override
    public @NotNull Location getCorner2() {
        return null;
    }

    @Override
    public @NotNull Location getSpawn2() {
        return null;
    }

    @Override
    public @NotNull String getName() {
        return "";
    }

    @NotNull
    @Override
    public void generate() {

    }

    @Override
    public @NotNull ArenaType getArenaType() {
        return null;
    }
}
