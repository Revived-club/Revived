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
