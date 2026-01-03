package club.revived.duels.database;

import club.revived.duels.database.provider.ArenaSchematicProvider;
import club.revived.duels.game.arena.schematic.WorldEditSchematic;
import club.revived.lobby.Lobby;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class DatabaseManager {

    private static DatabaseManager manager;
    private boolean isConnected = false;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private final Map<Class<?>, DatabaseProvider<?>> providers = new HashMap<>();

    public DatabaseManager() {
        manager = this;
        connect();
    }

    public void connect() {
        try {
            final var configFile = new File(Lobby.getInstance().getDataFolder(), "mongo.yml");
            final var config = YamlConfiguration.loadConfiguration(configFile);

            final MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(config.getString("connectionString", "mongodb://localhost:27017")))
                    .build();

            this.mongoClient = MongoClients.create(settings);
            this.database = mongoClient.getDatabase("revived");
            this.isConnected = true;
        } catch (final Exception e) {
            this.isConnected = false;
            destroy();
            throw new IllegalStateException(e);
        }

        register();
    }

    /**
     * Closes the MongoDB client and releases associated resources.
     *
     * If no client is initialized, this method has no effect.
     */
    public void destroy() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    /**
     * Retrieve an entity of the specified class using the provided string key.
     *
     * @param clazz the entity class to fetch
     * @param key   the string key identifying the entity (e.g., document id)
     * @return      an Optional containing the entity if found, or empty otherwise
     */
    public <T> CompletableFuture<Optional<T>> get(Class<T> clazz, String key) {
        if (!isConnected) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> (Optional<T>) providers.get(clazz).get(key));
    }

    /**
     * Saves the given entity instance for the specified entity class.
     *
     * @param clazz the entity class whose provider will handle the save
     * @param t the entity instance to persist
     * @return a CompletableFuture that completes with null after the save operation finishes; completes immediately with null if the database is not connected
     */
    public <T> CompletableFuture<Void> save(Class<T> clazz, T t) {
        if (!isConnected) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            ((DatabaseProvider<T>) providers.get(clazz)).save(t);
            return null;
        });
    }

    /**
     * Registers and starts database providers for kit-related entities.
     *
     * Initializes the provider registry with implementations for KitHolder and KitRoomPage
     * backed by the current MongoDB database, then invokes start() on each registered provider.
     */
    private void register() {
        this.providers.put(WorldEditSchematic.class, new ArenaSchematicProvider(this.database));

        for (final DatabaseProvider<?> provider : providers.values()) {
            provider.start();
        }
    }

    public static DatabaseManager getInstance() {
        if (manager == null) {
            new DatabaseManager();
            return manager;
        }
        return manager;
    }
}