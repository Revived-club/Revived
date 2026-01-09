package club.revived.duels.game.arena.schematic;

import club.revived.duels.database.DatabaseManager;
import club.revived.duels.game.arena.ArenaType;
import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.messaging.impl.UpdateArenas;

import java.util.ArrayList;
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
        this.loadArenas();

        Cluster.getInstance().getMessagingService().registerMessageHandler(UpdateArenas.class, _ -> this.loadArenas());
    }

    /**
     * Loads all arenas from the database & updates the schematics & worldedit schematics
     */
    private void loadArenas() {
        DatabaseManager.getInstance().getAll(WorldeditSchematic.class)
                .thenAccept(worldeditSchematics1 -> {
                    for (final var schem : worldeditSchematics1) {
                        this.worldeditSchematics.put(schem.id(), schem);
                    }
                });

        DatabaseManager.getInstance().getAll(DuelArenaSchematic.class)
                .thenAccept(arenaSchematics -> {
                    final var restricted = new ArrayList<>(arenaSchematics)
                            .stream()
                            .filter(duelArenaSchematic -> duelArenaSchematic.arenaType() == ArenaType.RESTRICTED)
                            .toList();

                    this.schematics.put(ArenaType.RESTRICTED, restricted);

                    final var interactive = new ArrayList<>(arenaSchematics)
                            .stream()
                            .filter(duelArenaSchematic -> duelArenaSchematic.arenaType() == ArenaType.INTERACTIVE)
                            .toList();

                    this.schematics.put(ArenaType.INTERACTIVE, interactive);
                });
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
     * <p>
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