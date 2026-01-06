package club.revived.duels.database;

import club.revived.duels.database.provider.ArenaSchematicProvider;
import club.revived.duels.database.provider.DuelArenaSchematicProvider;
import club.revived.duels.database.provider.DuelKitProvider;
import club.revived.duels.database.provider.EditedDuelKitProvider;
import club.revived.duels.game.arena.schematic.DuelArenaSchematic;
import club.revived.duels.game.arena.schematic.WorldeditSchematic;
import club.revived.duels.game.kit.DuelKit;
import club.revived.duels.game.kit.EditedDuelKit;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

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

    /**
     * Creates the DatabaseManager singleton and initiates a connection to the configured MongoDB.
     *
     * The newly constructed instance is published as the class-wide singleton and immediately
     * attempts to establish the database connection and register providers.
     *
     * @throws IllegalStateException if establishing the MongoDB connection fails
     */
    public DatabaseManager() {
        manager = this;
    }

    /**
     * Establishes a MongoDB connection and initializes database providers.
     *
     * Reads connection settings from "mongo.yml" in the plugin data folder (key
     * `connectionString`, defaulting to "mongodb://localhost:27017"), creates a
     * MongoClient with those settings, selects the "revived" database, and marks
     * the manager as connected. After a successful connection, calls {@code register()}
     * to initialize and start per-class providers.
     *
     * @throws IllegalStateException if establishing the MongoDB connection fails;
     *         the manager's connection state will be set to false and resources
     *         will be destroyed before the exception is thrown
     */
    public void connect(
            final String host,
            final int port,
            final String username,
            final String password,
            final String database
    ) {
        try {
            final String connectionString;

            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                connectionString = String.format("mongodb://%s:%s@%s:%d", username, password, host, port);
            } else {
                connectionString = String.format("mongodb://%s:%d", host, port);
            }

            final MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .build();

            this.mongoClient = MongoClients.create(settings);
            this.database = mongoClient.getDatabase(database);
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
     * Persists the provided entity using the registered provider for the given entity class.
     *
     * @param clazz the entity class whose provider will handle the save
     * @param t the entity instance to persist
     * @return `null` when the operation completes
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
     * Initializes the provider registry with database-backed providers for WorldeditSchematic,
     * DuelArenaSchematic, DuelKit, and EditedDuelKit, then starts each registered provider.
     */
    private void register() {
        this.providers.put(WorldeditSchematic.class, new ArenaSchematicProvider(this.database));
        this.providers.put(DuelArenaSchematic.class, new DuelArenaSchematicProvider(this.database));
        this.providers.put(DuelKit.class, new DuelKitProvider(this.database));
        this.providers.put(EditedDuelKit.class, new EditedDuelKitProvider(this.database));

        for (final DatabaseProvider<?> provider : providers.values()) {
            provider.start();
        }
    }

    /**
     * Get the singleton DatabaseManager instance, creating it if none exists.
     *
     * @return the singleton DatabaseManager instance
     */
    public static DatabaseManager getInstance() {
        if (manager == null) {
            new DatabaseManager();
            return manager;
        }
        return manager;
    }
}