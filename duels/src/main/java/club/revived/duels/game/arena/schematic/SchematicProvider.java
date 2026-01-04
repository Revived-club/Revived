package club.revived.duels.game.arena.schematic;

import club.revived.duels.game.arena.ArenaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SchematicProvider
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class SchematicProvider {

    private final Map<ArenaType, List<DuelArenaSchematic>> schematics = new HashMap<>();
    private final Map<String, WorldeditSchematic> worldeditSchematics = new HashMap<>();

    private static SchematicProvider schematicProvider;

    public SchematicProvider() {
        schematicProvider = this;
    }

    public Map<ArenaType, List<DuelArenaSchematic>> getSchematics() {
        return schematics;
    }

    public Map<String, WorldeditSchematic> getWorldeditSchematics() {
        return worldeditSchematics;
    }

    public static SchematicProvider getInstance() {
        if (schematicProvider == null) {
            return new SchematicProvider();
        }

        return schematicProvider;
    }
}
