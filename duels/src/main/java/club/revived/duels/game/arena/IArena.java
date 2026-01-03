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

    @NotNull
    Location getCorner1();

    @NotNull
    Location getSpawn1();

    @NotNull
    Location getCorner2();

    @NotNull
    Location getSpawn2();

    @NotNull
    String getId();

    void generate();

    @NotNull
    ArenaType getArenaType();

}
