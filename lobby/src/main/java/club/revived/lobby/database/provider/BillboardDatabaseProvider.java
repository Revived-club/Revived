package club.revived.lobby.database.provider;

import club.revived.commons.adapter.LocationTypeAdapter;
import club.revived.commons.serialization.LocationSerializer;
import club.revived.lobby.database.DatabaseProvider;
import club.revived.lobby.game.billboard.QueueBillboardLocation;
import club.revived.lobby.game.duel.KitType;
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
 * MongoDB-backed {@link DatabaseProvider} for {@link QueueBillboardLocation} instances.
 *
 * <p>Each billboard is uniquely identified by its {@link KitType} and stored in the
 * {@code queue_billboards} collection.</p>
 *
 * <p>Document schema:</p>
 * <ul>
 *   <li><b>_id</b> – {@link String}, the {@link KitType#name()}</li>
 *   <li><b>type</b> – {@link String}, redundant storage of the {@link KitType}</li>
 *   <li><b>location</b> – {@link String}, serialized {@link Location}</li>
 * </ul>
 *
 * <p>Unlike {@link LocationDatabaseProvider}, deserialization failures are handled
 * defensively and result in an empty {@link Optional} rather than an exception.</p>
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class BillboardDatabaseProvider implements DatabaseProvider<QueueBillboardLocation> {

    private final MongoCollection<Document> collection;
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Location.class, new LocationTypeAdapter())
            .create();


    /**
     * Constructs the provider and ensures the {@code queue_billboards} collection exists.
     *
     * @param database the MongoDB database instance
     */
    public BillboardDatabaseProvider(final MongoDatabase database) {
        database.createCollection("queue_billboards");
        this.collection = database.getCollection("queue_billboards");
    }

    /**
     * Performs provider initialization.
     *
     * <p>Creates an index to optimize billboard lookups by identifier.</p>
     */
    @Override
    public void start() {
        collection.createIndex(new Document("id", 1));
    }

    /**
     * Persists the given {@link QueueBillboardLocation}.
     *
     * <p>The associated {@link KitType} is used as the document identifier.
     * Existing documents are replaced atomically.</p>
     *
     * @param queueBillboardLocation the billboard location to store
     */
    @Override
    public void save(final QueueBillboardLocation queueBillboardLocation) {
        final Document doc = new Document()
                .append("_id", queueBillboardLocation.kitType().name())
                .append("type", queueBillboardLocation.kitType().name())
                .append("location", LocationSerializer.serialize(queueBillboardLocation.location()));

        collection.replaceOne(
                Filters.eq("_id", queueBillboardLocation.kitType().name()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    /**
     * Retrieves a {@link QueueBillboardLocation} by its identifier.
     *
     * @param id the document {@code _id}, expected to match a {@link KitType#name()}
     * @return an {@link Optional} containing the decoded {@link QueueBillboardLocation},
     *         or {@link Optional#empty()} if no document exists or deserialization fails
     */
    @NotNull
    @Override
    public Optional<QueueBillboardLocation> get(final String id) {
        final Document doc = collection.find(Filters.eq("_id", id)).first();

        if (doc == null) {
            return Optional.empty();
        }

        try {
            final KitType type = KitType.valueOf(doc.getString("type"));
            final Location location = LocationSerializer.deserialize(doc.getString("location"));

            return Optional.of(new QueueBillboardLocation(type, location));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public @NotNull List<QueueBillboardLocation> getAll() {
        final List<QueueBillboardLocation> result = new ArrayList<>();

        for (final Document doc : this.collection.find()) {
            try {
                final KitType type = KitType.valueOf(doc.getString("type"));
                final Location location = LocationSerializer.deserialize(doc.getString("location"));

                result.add(new QueueBillboardLocation(type, location));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
