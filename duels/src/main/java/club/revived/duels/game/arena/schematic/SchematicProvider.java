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

    /**
     * Creates a new SchematicProvider and registers it as the global singleton instance.
     *
     * <p>This constructor assigns the created instance to the class's static
     * singleton reference so subsequent calls to {@link #getInstance()} return this instance.</p>
     */
    public SchematicProvider() {
        schematicProvider = this;
    }

    /**
     * Provides access to the stored schematics organized by arena type.
     *
     * @return the map from {@code ArenaType} to the corresponding list of {@code DuelArenaSchematic}
     */
    public Map<ArenaType, List<DuelArenaSchematic>> getSchematics() {
        return schematics;
    }

    /**
     * Provides access to the registry of WorldEdit schematics by identifier.
     *
     * @return the map of WorldeditSchematic objects keyed by their string identifiers
     */
    public Map<String, WorldeditSchematic> getWorldeditSchematics() {
        return worldeditSchematics;
    }

    /**
     * Retrieves the singleton SchematicProvider instance, creating and storing one if none exists.
     *
     * Note: this method is not thread-safe.
     *
     * @return the shared SchematicProvider instance; created if it did not already exist
     */
    public static SchematicProvider getInstance() {
        if (schematicProvider == null) {
            return new SchematicProvider();
        }

        return schematicProvider;
    }
}