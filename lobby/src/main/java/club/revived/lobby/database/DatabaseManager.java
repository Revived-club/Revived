package club.revived.lobby.database;

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

    public void destroy() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    public <T> CompletableFuture<Optional<T>> get(Class<T> clazz, UUID uuid) {
        if (!isConnected) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> (Optional<T>) providers.get(clazz).get(uuid));
    }

    public <T> CompletableFuture<Void> save(Class<T> clazz, T t) {
        if (!isConnected) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            ((DatabaseProvider<T>) providers.get(clazz)).save(t);
            return null;
        });
    }

    private void register() {

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