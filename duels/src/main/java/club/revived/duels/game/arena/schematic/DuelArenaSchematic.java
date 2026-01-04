package club.revived.duels.game.arena.schematic;

import club.revived.duels.game.arena.ArenaType;
import org.bukkit.Location;

/**
 * DuelArenaSchematic
 *
 * @author yyuh
 * @since 04.01.26
 */
public record DuelArenaSchematic(
        String id,
        Location corner1,
        Location corner2,
        Location spawn1,
        Location spawn2,
        ArenaType arenaType
) {}
