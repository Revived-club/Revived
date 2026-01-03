package club.revived.duels.database.provider;

import club.revived.duels.Duels;
import club.revived.duels.database.DatabaseProvider;
import club.revived.duels.game.arena.schematic.WorldEditSchematic;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.Binary;
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
public final class ArenaSchematicProvider implements DatabaseProvider<WorldEditSchematic> {

    private final MongoCollection<Document> collection;

    public ArenaSchematicProvider(final MongoDatabase database) {
        database.createCollection("schematics");
        this.collection = database.getCollection("schematics");
    }

    @Override
    public void start() {
        collection.createIndex(new Document("id", 1));
    }

    @Override
    public void save(final WorldEditSchematic worldEditSchematic) {
        try {
            final byte[] bytes = Files.readAllBytes(worldEditSchematic.file().toPath());

            final var doc = new Document("id", worldEditSchematic.id())
                    .append("data", new Binary(bytes));

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
    public @NotNull Optional<WorldEditSchematic> get(final String key) {
        try {
            final var doc = this.collection.find(
                    Filters.eq("id", key)
            ).first();

            if (doc == null) {
                return Optional.empty();
            }

            final var binary = doc.get("data", Binary.class);

            final File file = new File(Duels.getInstance().getDataFolder(), "/schem/" + key);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            Files.write(file.toPath(), binary.getData());

            return Optional.of(new WorldEditSchematic(key, file));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull List<WorldEditSchematic> getAll() {
        final List<WorldEditSchematic> result = new ArrayList<>();
        try {
            for (final Document doc : this.collection.find()) {
                final String id = doc.getString("id");
                final Binary binary = doc.get("data", Binary.class);

                final File file = new File(Duels.getInstance().getDataFolder(), "/schem/" + id);

                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                Files.write(file.toPath(), binary.getData());

                result.add(new WorldEditSchematic(id, file));
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
