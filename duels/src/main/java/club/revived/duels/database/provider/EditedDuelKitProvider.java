package club.revived.duels.database.provider;

import club.revived.commons.adapter.ItemStackTypeAdapter;
import club.revived.duels.database.DatabaseProvider;
import club.revived.duels.game.kit.EditedDuelKit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * EditedDuelKitProvider
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class EditedDuelKitProvider implements DatabaseProvider<EditedDuelKit> {

    private final MongoCollection<Document> collection;
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .create();

    public EditedDuelKitProvider(final MongoDatabase database) {
        database.createCollection("editedDuelKits");
        this.collection = database.getCollection("editedDuelKits");
    }

    @Override
    public void start() {
        collection.createIndex(new Document("uuid", 1));
    }

    @Override
    public void save(final EditedDuelKit duelKit) {
        try {
            final var uuid = duelKit.uuid();

            final var json = this.gson.toJson(duelKit.content());
            final var doc = new Document("uuid", uuid).append("data", json);

            collection.replaceOne(
                    Filters.eq("uuid", uuid),
                    doc,
                    new ReplaceOptions().upsert(true)
            );
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Optional<EditedDuelKit> get(final String key) {
        try {
            final var doc = this.collection.find(
                    Filters.eq("uuid", key)
            ).first();

            if (doc == null) {
                return Optional.empty();
            }

            final var json = doc.getString("data");
            final var kit = this.gson.fromJson(json, EditedDuelKit.class);

            return Optional.of(kit);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull List<EditedDuelKit> getAll() {
        throw new UnsupportedOperationException();
    }
}
