package club.revived.lobby.database.provider;

import club.revived.commons.location.SerializedLocation;
import club.revived.commons.serialization.LocationSerializer;
import club.revived.lobby.database.DatabaseProvider;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB-backed {@link DatabaseProvider} responsible for persisting and retrieving
 * {@link SerializedLocation} instances.
 *
 * <p>Locations are stored in the {@code warp_locations} collection using the following schema:</p>
 *
 * <ul>
 *   <li><b>_id</b> – {@link String}, the unique identifier of the location</li>
 *   <li><b>content</b> – {@link String}, the serialized {@link org.bukkit.Location}</li>
 * </ul>
 *
 * <p>Writes are performed using an upsert strategy, ensuring that saves are idempotent.</p>
 *
 * <p>This provider assumes that {@link LocationSerializer} is stable and backward-compatible.
 * Deserialization failures are treated as fatal and will propagate as runtime exceptions.</p>
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class LocationDatabaseProvider implements DatabaseProvider<SerializedLocation> {

    private final MongoCollection<Document> collection;

    /**
     * Constructs the provider and ensures the {@code warp_locations} collection exists.
     *
     * @param database the MongoDB database instance
     */
    public LocationDatabaseProvider(final MongoDatabase database) {
        database.createCollection("warp_locations");
        this.collection = database.getCollection("warp_locations");
    }

    /**
     * Performs provider initialization.
     *
     * <p>Creates an index on the {@code id} field to improve lookup performance.</p>
     * <p>This method is expected to be invoked once during application startup.</p>
     */
    @Override
    public void start() {
        collection.createIndex(new Document("id", 1));
    }

    /**
     * Persists the given {@link SerializedLocation}.
     *
     * <p>If a document with the same identifier already exists, it will be replaced.
     * Otherwise, a new document is inserted.</p>
     *
     * @param serializedLocation the location wrapper to store
     */
    @Override
    public void save(final SerializedLocation serializedLocation) {
        final Document doc = new Document()
                .append("_id", serializedLocation.id())
                .append("content", LocationSerializer.serialize(serializedLocation.location()));

        collection.replaceOne(
                Filters.eq("_id", serializedLocation.id()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    /**
     * Retrieves a {@link SerializedLocation} by its identifier.
     *
     * @param id the unique location identifier
     * @return an {@link Optional} containing the decoded {@link SerializedLocation},
     *         or {@link Optional#empty()} if no document exists
     * @throws RuntimeException if deserialization fails due to corrupted or invalid data
     */
    @Override
    public @NonNull Optional<SerializedLocation> get(final String id) {
        final Document doc = collection.find(Filters.eq("_id", id)).first();

        if (doc == null) {
            return Optional.empty();
        }

        try {
            final String serializedContent = doc.getString("content");
            return Optional.of(new SerializedLocation(
                    id,
                    LocationSerializer.deserialize(serializedContent)
            ));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to deserialize location with id: " + id, e);
        }
    }

    @Override
    public @NotNull List<SerializedLocation> getAll() {
        return List.of();
    }
}
