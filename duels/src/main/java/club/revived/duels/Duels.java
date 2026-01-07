package club.revived.duels;

import club.revived.commons.inventories.impl.InventoryManager;
import club.revived.duels.database.DatabaseManager;
import club.revived.duels.game.chat.listener.PlayerChatListener;
import club.revived.duels.game.duels.listener.PlayerListener;
import club.revived.duels.service.broker.RedisBroker;
import club.revived.duels.service.cache.RedisCacheService;
import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.cluster.ServiceType;
import club.revived.duels.service.player.PlayerManager;
import club.revived.duels.service.status.ServiceStatus;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Duels
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Duels extends JavaPlugin {

    private static Duels instance;


    /**
     * Perform startup initialization for the plugin.
     *
     * Sets the singleton instance, registers the inventory manager, configures cluster and database connections,
     * initializes player management and chat listener, and marks the cluster as available.
     */
    @Override
    public void onEnable() {
        instance = this;

        InventoryManager.register(this);

        this.setupCluster();
        this.connectDatabase();

        new PlayerListener();
        new PlayerManager();
        new PlayerChatListener();

        Cluster.STATUS = ServiceStatus.AVAILABLE;
    }

    /**
     * Performs plugin load-time initialization.
     */
    @Override
    public void onLoad() {
        super.onLoad();
    }

    /**
     * Called when the plugin is disabled.
     *
     * <p>Currently this implementation performs no actions.</p>
     */
    @Override
    public void onDisable() {
        Cluster.STATUS = ServiceStatus.SHUTTING_DOWN;
    }

    /**
     * Initializes the application's Cluster integration using Redis and environment configuration.
     *
     * Reads the environment variables `HOSTNAME`, `REDIS_HOST`, and `REDIS_PORT` and constructs a
     * Cluster configured with a Redis broker and Redis cache service for ServiceType.LOBBY.
     */
    private void setupCluster() {
        final String hostName = System.getenv("HOSTNAME");
        final String host = System.getenv("REDIS_HOST");
        final int port = Integer.parseInt(System.getenv("REDIS_PORT"));

        new Cluster(
                new RedisBroker(host, port, ""),
                new RedisCacheService(host, port, ""),
                ServiceType.LOBBY,
                hostName
        );
    }

    /**
     * Establishes a connection to the configured MongoDB instance using environment variables.
     *
     * Reads MONGODB_HOST, MONGODB_USERNAME, MONGODB_PASSWORD, and MONGODB_DATABASE to configure the connection and connects on port 27017.
     */
    private void connectDatabase() {
        final String host = System.getenv("MONGODB_HOST");
        final String password = System.getenv("MONGODB_PASSWORD");
        final String username = System.getenv("MONGODB_USERNAME");
        final String database = System.getenv("MONGODB_DATABASE");

        DatabaseManager.getInstance().connect(
                host,
                27017,
                username,
                password,
                database
        );
    }

    /**
     * Accesses the singleton Duels instance.
     *
     * @return the shared Duels instance, or null if the plugin has not been enabled.
     */
    public static Duels getInstance() {
        return instance;
    }
}