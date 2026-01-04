package club.revived.duels.database.provider;

import club.revived.commons.adapter.LocationTypeAdapter;
import club.revived.duels.Duels;
import club.revived.duels.database.DatabaseProvider;
import club.revived.duels.game.arena.schematic.DuelArenaSchematic;
import club.revived.duels.game.arena.schematic.WorldeditSchematic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.Binary;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
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

    public DuelArenaSchematicProvider(final MongoDatabase database) {
        database.createCollection("schematics");
        this.collection = database.getCollection("schematics");
    }

    @Override
    public void start() {
        collection.createIndex(new Document("id", 1));
    }

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
