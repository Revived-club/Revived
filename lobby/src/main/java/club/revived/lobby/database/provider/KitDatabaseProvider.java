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

    public KitDatabaseProvider(final MongoDatabase database) {
        database.createCollection("kits");
        this.collection = database.getCollection("kits");
    }

    @Override
    public void start() {
        collection.createIndex(new Document("uuid", 1));
    }

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
