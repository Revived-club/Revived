package club.revived.lobby.game.duel.schematic;

import org.bukkit.Location;

/**
 * DuelArenaDraft
 *
 * @author yyuh
 * @since 09.01.26
 */
public final class DuelArenaDraft {

    private final String id;
    private final Location corner1;
    private final Location corner2;
    private final ArenaType arenaType;

    private Location spawn1;
    private Location spawn2;

    public DuelArenaDraft(
            String id,
            Location corner1,
            Location corner2,
            ArenaType arenaType
    ) {
        this.id = id;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.arenaType = arenaType;
    }

    public boolean isComplete() {
        return spawn1 != null && spawn2 != null;
    }

    public DuelArenaSchematic toArena() {
        return new DuelArenaSchematic(
                id,
                corner1,
                corner2,
                spawn1,
                spawn2,
                arenaType
        );
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }


    public String getId() {
        return id;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public ArenaType getArenaType() {
        return arenaType;
    }

    public Location getSpawn1() {
        return spawn1;
    }

    public Location getSpawn2() {
        return spawn2;
    }

}
