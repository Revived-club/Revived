package club.revived.lobby;

import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.command.DuelCommand;
import club.revived.lobby.service.broker.RedisBroker;
import club.revived.lobby.service.cache.RedisCacheService;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.player.PlayerManager;
import club.revived.lobby.service.status.ServiceStatus;
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
     * Performs plugin startup: sets the singleton instance and runs startup initialization.
     *
     * <p>Establishes the static plugin instance, then initializes the database connection,
     * registers commands, and configures cluster services.</p>
     */
    @Override
    public void onEnable() {
        instance = this;

        this.connectDatabase();
        this.setupCommands();
        this.setupCluster();

        new PlayerManager();

        Cluster.STATUS = ServiceStatus.AVAILABLE;
    }


    /**
     * Initializes and registers the plugin's command handlers for the lobby.
     *
     * Specifically instantiates and registers the DuelCommand. 
     */
    private void setupCommands() {
        new DuelCommand();
    }


    /**
     * Initializes a MongoDB connection from environment-provided credentials and registers it with the DatabaseManager.
     *
     * Reads the environment variables `MONGODB_HOST`, `MONGODB_USERNAME`, `MONGODB_PASSWORD`, and `MONGODB_DATABASE`
     * and connects to the specified host on port 27017.
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
     * Initializes the plugin Cluster using environment variables.
     *
     * Reads HOSTNAME, REDIS_HOST, and REDIS_PORT from the process environment and constructs
     * a Cluster configured with a RedisBroker and RedisCacheService using those values.
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
     * Accesses the singleton Lobby instance.
     *
     * @return the shared Lobby instance, or null if the plugin has not been enabled.
     */
    public static Lobby getInstance() {
        return instance;
    }
}