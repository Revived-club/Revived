package club.revived.lobby.database.provider;

import club.revived.commons.adapter.LocationTypeAdapter;
import club.revived.lobby.database.DatabaseProvider;
import club.revived.lobby.game.friends.FriendHolder;
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
 * FriendDatabaseProvider
 *
 * @author yyuh
 * @since 15.01.26
 */
public final class FriendDatabaseProvider implements DatabaseProvider<FriendHolder> {

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
    public FriendDatabaseProvider(final MongoDatabase database) {
        database.createCollection("friends");
        this.collection = database.getCollection("friends");
    }

    /**
     * Ensures the schematics collection has an ascending index on the `id` field to optimize lookups.
     */
    @Override
    public void start() {
        collection.createIndex(new Document("uuid", 1));
    }

    @Override
    public void save(final FriendHolder friendHolder) {
        try {
            System.out.println("Saving " + friendHolder.uuid());

            final var json = this.gson.toJson(friendHolder);

            final var doc = new Document("uuid", friendHolder.uuid())
                    .append("data", json);

            collection.replaceOne(
                    Filters.eq("uuid", friendHolder.uuid()),
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
    public @NotNull Optional<FriendHolder> get(final String key) {
        try {
            final var doc = this.collection.find(
                    Filters.eq("uuid", key)
            ).first();

            if (doc == null) {
                return Optional.empty();
            }

            final var json = doc.getString("data");
            final var schematic = this.gson.fromJson(json, FriendHolder.class);

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
    public @NotNull List<FriendHolder> getAll() {
        final List<FriendHolder> result = new ArrayList<>();
        try {
            for (final Document doc : this.collection.find()) {
                final var json = doc.getString("data");

                final var friendHolder = this.gson.fromJson(json, FriendHolder.class);

                result.add(friendHolder);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
