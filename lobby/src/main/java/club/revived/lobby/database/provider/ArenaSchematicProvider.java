package club.revived.lobby.database.provider;

import club.revived.lobby.Lobby;
import club.revived.lobby.database.DatabaseProvider;
import club.revived.lobby.game.duel.schematic.WorldeditSchematic;
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
public final class ArenaSchematicProvider implements DatabaseProvider<WorldeditSchematic> {

    private final MongoCollection<Document> collection;

    /**
     * Creates a provider backed by the supplied MongoDatabase and ensures a "schematics" collection exists.
     *
     * @param database the MongoDatabase used to store and retrieve schematic documents
     */
    public ArenaSchematicProvider(final MongoDatabase database) {
        database.createCollection("schematics");
        this.collection = database.getCollection("schematics");
    }

    /**
     * Initializes the provider by creating an ascending index on the collection's "id" field to optimize id-based queries.
     */
    @Override
    public void start() {
        collection.createIndex(new Document("id", 1));
    }

    /**
     * Stores the given WorldeditSchematic's file contents in the schematics collection under its id.
     *
     * @param worldEditSchematic the schematic whose file will be read and saved to the database
     * @throws RuntimeException if reading the schematic file or writing to the database fails
     */
    @Override
    public void save(final WorldeditSchematic worldEditSchematic) {
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

    /**
     * Loads the schematic with the given id from the database and writes its binary data to the plugin data folder.
     *
     * The schematic file is written to {@code <plugin-data>/schem/<id>} before the returned object is created.
     *
     * @param key the schematic id to retrieve
     * @return an {@link Optional} containing the {@link WorldeditSchematic} for the given id if found, {@code Optional.empty()} otherwise
     * @throws RuntimeException if a database access or file I/O error occurs while retrieving or writing the schematic
     */
    @Override
    public @NotNull Optional<WorldeditSchematic> get(final String key) {
        try {
            final var doc = this.collection.find(
                    Filters.eq("id", key)
            ).first();

            if (doc == null) {
                return Optional.empty();
            }

            final var binary = doc.get("data", Binary.class);

            final File file = new File(Lobby.getInstance().getDataFolder(), "/schem/" + key);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            Files.write(file.toPath(), binary.getData());

            return Optional.of(new WorldeditSchematic(key, file));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads every schematic stored in the MongoDB collection, writes each schematic's binary data
     * to a file under the plugin data folder at "schem/{id}", and returns a list of corresponding WorldeditSchematic objects.
     *
     * Each schematic's parent directories are created if missing and the file is overwritten with the stored binary data.
     *
     * @return a list of WorldeditSchematic objects reconstructed from each document in the collection
     * @throws RuntimeException if an I/O or database error occurs while reading documents or writing files
     */
    @Override
    public @NotNull List<WorldeditSchematic> getAll() {
        final List<WorldeditSchematic> result = new ArrayList<>();
        try {
            for (final Document doc : this.collection.find()) {
                final String id = doc.getString("id");
                final Binary binary = doc.get("data", Binary.class);

                final File file = new File(Lobby.getInstance().getDataFolder(), "/schem/" + id);

                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                Files.write(file.toPath(), binary.getData());

                result.add(new WorldeditSchematic(id, file));
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}