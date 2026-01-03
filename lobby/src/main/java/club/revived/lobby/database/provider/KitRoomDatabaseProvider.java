package club.revived.lobby.database.provider;

import club.revived.commons.adapter.ItemStackTypeAdapter;
import club.revived.lobby.database.DatabaseProvider;
import club.revived.lobby.game.kit.KitHolder;
import club.revived.lobby.game.kit.KitRoomPage;
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

/**
 * KitRoomDatabaseProvider
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class KitRoomDatabaseProvider implements DatabaseProvider<KitRoomPage> {

    private final MongoCollection<Document> collection;
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .create();

    public KitRoomDatabaseProvider(final MongoDatabase database) {
        database.createCollection("kitroom");
        this.collection = database.getCollection("kitroom");
    }

    @Override
    public void start() {
        collection.createIndex(new Document("id", 1));
    }

    @Override
    public void save(final KitRoomPage kitRoomPage) {
        try {
            final var id = kitRoomPage.type().toString();

            final var json = this.gson.toJson(kitRoomPage);
            final var doc = new Document("id", id)
                    .append("data", json);

            collection.replaceOne(
                    Filters.eq("id", id),
                    doc,
                    new ReplaceOptions().upsert(true)
            );
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Optional<KitRoomPage> get(final String key) {
        try {
            final var doc = this.collection.find(
                    Filters.eq("id", key)
            ).first();

            if (doc == null) {
                return Optional.empty();
            }

            final var json = doc.getString("data");
            final var kit = this.gson.fromJson(json, KitRoomPage.class);

            return Optional.of(kit);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
