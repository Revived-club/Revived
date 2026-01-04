package club.revived.lobby;

import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.command.DuelCommand;
import club.revived.lobby.service.broker.RedisBroker;
import club.revived.lobby.service.cache.RedisCacheService;
import club.revived.lobby.service.cluster.Cluster;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Lobby extends JavaPlugin {

    private static Lobby instance;

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

    }

    /**
     * Initializes the plugin: sets the singleton instance, reads the HOSTNAME environment variable into {@code hostName}, and configures the cluster.
     *
     * @throws RuntimeException if retrieving the HOSTNAME environment variable fails
     */
    @Override
    public void onEnable() {
        instance = this;

        this.connectDatabase();
        this.setupCommands();
        this.setupCluster();
    }


    private void setupCommands() {
        new DuelCommand();
    }


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
     * Initializes the application's Cluster from the plugin's redis.yml configuration.
     * <p>
     * Loads host, port, and password from redis.yml in the plugin data folder and constructs
     * a Cluster using RedisBroker and RedisCacheService configured with those values and the
     * plugin's hostName.
     */
    private void setupCluster() {
        final String hostName = System.getenv("HOSTNAME");
        final String host = System.getenv("REDIS_HOST");
        final int port = Integer.parseInt(System.getenv("REDIS_PORT"));

        new Cluster(
                new RedisBroker(host, port, ""),
                new RedisCacheService(host, port, ""),
                hostName
        );
    }

    /**
     * Accesses the singleton Lobby instance.
     *
     * @return the shared Lobby instance, or null if the plugin has not been enabled.
     */
    public static Lobby getInstance() {
        return instance;
    }
}