package club.revived.lobby.database.provider;

import club.revived.commons.adapter.ItemStackTypeAdapter;
import club.revived.lobby.database.DatabaseProvider;
import club.revived.lobby.game.kit.KitHolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * KitDatabaseProvider
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class KitDatabaseProvider implements DatabaseProvider<KitHolder> {

    private final MongoCollection<Document> collection;
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .create();

    /**
     * Creates a KitDatabaseProvider and ensures the MongoDB collection named "kits" exists.
     *
     * @param database the MongoDatabase used to create and access the "kits" collection
     */
    public KitDatabaseProvider(final MongoDatabase database) {
        database.createCollection("kits");
        this.collection = database.getCollection("kits");
    }

    /**
     * Ensure the collection has an ascending index on the "uuid" field.
     *
     * This creates an index to improve lookup performance for operations that query kits by UUID.
     */
    @Override
    public void start() {
        collection.createIndex(new Document("uuid", 1));
    }

    /**
     * Persists the given KitHolder into the MongoDB "kits" collection.
     *
     * The KitHolder is serialized to JSON and stored in a document with fields
     * "uuid" (the holder's UUID as a string) and "data" (the serialized JSON).
     *
     * @param kitHolder the KitHolder to persist
     * @throws RuntimeException if an error occurs while serializing or writing to the database
     */
    @Override
    public void save(final KitHolder kitHolder) {
        try {
            final var uuid = kitHolder.uuid();

            final var json = this.gson.toJson(kitHolder);
            final var doc = new Document("uuid", uuid.toString())
                    .append("data", json);

            collection.replaceOne(
                    Filters.eq("uuid", uuid.toString()),
                    doc,
                    new ReplaceOptions().upsert(true)
            );
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve a KitHolder by its UUID string from the kits collection.
     *
     * @param key the UUID of the KitHolder to fetch, as a string
     * @return an Optional containing the deserialized KitHolder if found, otherwise Optional.empty()
     * @throws RuntimeException if a database or deserialization error occurs
     */
    @Override
    public @NotNull Optional<KitHolder> get(final @NotNull String key) {
        try {
             final var doc = this.collection.find(
                     Filters.eq("uuid", key)
             ).first();

             if (doc == null) {
                 return Optional.empty();
             }

             final var json = doc.getString("data");
             final var kit = this.gson.fromJson(json, KitHolder.class);

             return Optional.of(kit);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}