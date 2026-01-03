package club.revived.duels.game.arena.schematic;

import club.revived.duels.game.arena.ArenaType;

import java.util.HashMap;
import java.util.Map;

/**
 * SchematicProvider
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class SchematicProvider {

    private final Map<ArenaType, WorldEditSchematic> schematics = new HashMap<>();


    public SchematicProvider() {

    }
}
