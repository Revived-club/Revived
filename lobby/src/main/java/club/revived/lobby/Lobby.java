package club.revived.lobby;

import club.revived.lobby.service.broker.RedisBroker;
import club.revived.lobby.service.cache.RedisCacheService;
import club.revived.lobby.service.cluster.Cluster;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Lobby extends JavaPlugin {

    private static Lobby instance;

    private String hostName;

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

        try {
            // Kubernetes system env variable
            this.hostName = System.getenv("HOSTNAME");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        setupCluster();
    }

    /**
     * Initializes the application's Cluster from the plugin's redis.yml configuration.
     *
     * Loads host, port, and password from redis.yml in the plugin data folder and constructs
     * a Cluster using RedisBroker and RedisCacheService configured with those values and the
     * plugin's hostName.
     */
    private void setupCluster() {
        final var redisFile = new File(getDataFolder(), "redis.yml");
        final var redisConfig = YamlConfiguration.loadConfiguration(redisFile);

        final String password = redisConfig.getString("password");
        final String host = redisConfig.getString("host");
        final int port = redisConfig.getInt("port");

        new Cluster(
                new RedisBroker(host, port, password),
                new RedisCacheService(host, port, password),
                this.hostName
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