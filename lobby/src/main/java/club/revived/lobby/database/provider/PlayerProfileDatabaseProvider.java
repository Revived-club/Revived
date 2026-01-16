package club.revived.lobby.database.provider;

import club.revived.lobby.database.DatabaseProvider;
import club.revived.lobby.game.player.PlayerProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
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
public final class PlayerProfileDatabaseProvider implements DatabaseProvider<PlayerProfile> {

    private final MongoCollection<Document> collection;

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();

    /**
     * Initializes the provider and ensures a MongoDB collection named "schematics" exists for storing duel arena schematics.
     *
     * @param database the MongoDatabase to use for persistence
     */
    public PlayerProfileDatabaseProvider(final MongoDatabase database) {
        database.createCollection("playerProfiles");
        this.collection = database.getCollection("playerProfiles");
    }

    /**
     * Ensures the schematics collection has an ascending index o   n the `id` field to optimize lookups.
     */
    @Override
    public void start() {
        collection.createIndex(new Document("uuid", 1));
    }

    @Override
    public void save(final PlayerProfile playerProfile) {
        try {
            final var json = this.gson.toJson(playerProfile);

            final var doc = new Document("uuid", playerProfile.uuid().toString())
                    .append("data", json);

            collection.replaceOne(
                    Filters.eq("uuid", playerProfile.uuid().toString()),
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
    public @NotNull Optional<PlayerProfile> get(final String key) {
        try {
            final var doc = this.collection.find(
                    Filters.eq("uuid", key)
            ).first();

            if (doc == null) {
                return Optional.empty();
            }

            final var json = doc.getString("data");
            final var playerProfile = this.gson.fromJson(json, PlayerProfile.class);

            return Optional.of(playerProfile);
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
    public @NotNull List<PlayerProfile> getAll() {
        final List<PlayerProfile> result = new ArrayList<>();
        try {
            for (final Document doc : this.collection.find()) {
                final var json = doc.getString("data");

                final var playerProfile = this.gson.fromJson(json, PlayerProfile.class);

                result.add(playerProfile);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
