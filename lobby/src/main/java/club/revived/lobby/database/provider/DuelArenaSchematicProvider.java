package club.revived.lobby.database.provider;

import club.revived.commons.adapter.LocationTypeAdapter;
import club.revived.lobby.database.DatabaseProvider;
import club.revived.lobby.game.duel.schematic.DuelArenaSchematic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ArenaSchematicProvider
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class DuelArenaSchematicProvider implements DatabaseProvider<DuelArenaSchematic> {

    private final MongoCollection<Document> collection;

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Location.class, new LocationTypeAdapter())
            .create();

    /**
     * Initializes the provider and ensures a MongoDB collection named "schematics" exists for storing duel arena schematics.
     *
     * @param database the MongoDatabase to use for persistence
     */
    public DuelArenaSchematicProvider(final MongoDatabase database) {
        database.createCollection("arenaSchematics");
        this.collection = database.getCollection("arenaSchematics");
    }

    /**
     * Ensures the schematics collection has an ascending index on the `id` field to optimize lookups.
     */
    @Override
    public void start() {
        collection.createIndex(new Document("id", 1));
    }

    /**
     * Persists the given duel arena schematic to the MongoDB collection, inserting or replacing by id.
     *
     * @param worldEditSchematic the schematic to persist; its {@code id()} value is used as the document key
     * @throws RuntimeException if serialization or the database operation fails
     */
    @Override
    public void save(final DuelArenaSchematic worldEditSchematic) {
        try {
            final var json = this.gson.toJson(worldEditSchematic);

            final var doc = new Document("id", worldEditSchematic.id())
                    .append("data", json);

            collection.replaceOne(
                    Filters.eq("id", worldEditSchematic.id()),
                    doc,
                    new ReplaceOptions().upsert(true)
            );
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve a duel arena schematic by its identifier.
     *
     * @param key the schematic's identifier to look up
     * @return an Optional containing the DuelArenaSchematic if found, or {@link Optional#empty()} if not
     */
    @Override
    public @NotNull Optional<DuelArenaSchematic> get(final String key) {
        try {
            final var doc = this.collection.find(
                    Filters.eq("id", key)
            ).first();

            if (doc == null) {
                return Optional.empty();
            }

            final var json = doc.getString("data");
            final var schematic = this.gson.fromJson(json, DuelArenaSchematic.class);

            return Optional.of(schematic);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load all DuelArenaSchematic objects stored in the schematics collection.
     *
     * @return a list of all deserialized DuelArenaSchematic instances; empty if no documents are present
     * @throws RuntimeException if a database access or deserialization error occurs
     */
    @Override
    public @NotNull List<DuelArenaSchematic> getAll() {
        final List<DuelArenaSchematic> result = new ArrayList<>();
        try {
            for (final Document doc : this.collection.find()) {
                final var json = doc.getString("data");

                final var schematic = this.gson.fromJson(json, DuelArenaSchematic.class);

                result.add(schematic);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}