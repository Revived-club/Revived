package club.revived.lobby.database;

import club.revived.lobby.Lobby;
import club.revived.lobby.database.provider.KitDatabaseProvider;
import club.revived.lobby.database.provider.KitRoomDatabaseProvider;
import club.revived.lobby.game.kit.KitHolder;
import club.revived.lobby.game.kit.KitRoomPage;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
     * Creates a DatabaseManager and registers it as the singleton instance.
     *
     * The constructor sets the static `manager` reference to this newly created instance.
     */
    public DatabaseManager() {
        manager = this;
    }

    /**
     * Establishes a MongoDB connection using the given host, port, optional credentials, and selects the specified database, then registers providers.
     *
     * If both `username` and `password` are non-null and non-empty, credentials are included in the connection string; otherwise the connection is attempted without authentication.
     *
     * @param host the MongoDB host address
     * @param port the MongoDB port
     * @param username optional username for authentication (may be null or empty to skip authentication)
     * @param password optional password for authentication (may be null or empty to skip authentication)
     * @param database the name of the database to select after connecting
     * @throws IllegalStateException if establishing the connection or initialization fails
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
        this.providers.put(KitHolder.class, new KitDatabaseProvider(this.database));
        this.providers.put(KitRoomPage.class, new KitRoomDatabaseProvider(this.database));

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