package club.revived.duels.database.provider;

import club.revived.commons.adapter.ItemStackTypeAdapter;
import club.revived.duels.database.DatabaseProvider;
import club.revived.duels.game.kit.DuelKit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DuelKitProvider
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class DuelKitProvider implements DatabaseProvider<DuelKit> {

    private final MongoCollection<Document> collection;
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .create();

    /**
     * Creates a DuelKitProvider backed by the given MongoDatabase.
     *
     * Initializes access to the "duelKits" collection by creating it and storing a reference for subsequent operations.
     *
     * @param database the MongoDatabase used to create and access the "duelKits" collection
     */
    public DuelKitProvider(final MongoDatabase database) {
        database.createCollection("duelKits");
        this.collection = database.getCollection("duelKits");
    }

    /**
     * Creates an ascending index on the collection's "id" field to optimize lookups by id.
     */
    @Override
    public void start() {
        collection.createIndex(new Document("id", 1));
    }

    /**
     * Persists the given DuelKit into the provider's storage.
     *
     * @param duelKit the DuelKit to persist; its type name is used as the document identifier
     * @throws RuntimeException if an error occurs while saving the DuelKit to the database
     */
    @Override
    public void save(final DuelKit duelKit) {
        try {
            final var id = duelKit.type().name();

            final var json = this.gson.toJson(duelKit.content());
            final var doc = new Document("id", id).append("data", json);

            collection.replaceOne(
                    Filters.eq("id", id),
                    doc,
                    new ReplaceOptions().upsert(true)
            );
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a DuelKit stored under the given id.
     *
     * @param key the duel kit id (derived from DuelKit.type().name())
     * @return an Optional containing the matching DuelKit, or Optional.empty() if no document is found
     */
    @Override
    public @NotNull Optional<DuelKit> get(final String key) {
        try {
            final var doc = this.collection.find(
                    Filters.eq("id", key)
            ).first();

            if (doc == null) {
                return Optional.empty();
            }

            final var json = doc.getString("data");
            final var kit = this.gson.fromJson(json, DuelKit.class);

            return Optional.of(kit);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve all DuelKit objects stored in the collection.
     *
     * Each document's "data" field is deserialized into a DuelKit and added to the returned list.
     *
     * @return a list containing every stored DuelKit; empty if none are found
     * @throws RuntimeException if reading from the database or deserializing any document fails
     */
    @Override
    public @NotNull List<DuelKit> getAll() {
        final List<DuelKit> result = new ArrayList<>();
        try {
            for (final Document doc : this.collection.find()) {
                final var json = doc.getString("data");
                final var duelKit = this.gson.fromJson(json, DuelKit.class);

                result.add(duelKit);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}