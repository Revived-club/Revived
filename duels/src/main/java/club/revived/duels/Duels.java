package club.revived.duels;

import club.revived.duels.service.broker.RedisBroker;
import club.revived.duels.service.cache.RedisCacheService;
import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.cluster.ServiceType;
import club.revived.duels.service.player.PlayerManager;
import club.revived.duels.service.status.ServiceStatus;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Duels
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Duels extends JavaPlugin {

    private static Duels instance;

    private String hostName;

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

        this.setupCluster();

        new PlayerManager();

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

    }

    /**
     * Initializes the Cluster from redis.yml in the plugin data folder.
     *
     * Reads "host", "port", and "password" from that file and constructs a Cluster configured with
     * RedisBroker and RedisCacheService using those values and this plugin's hostName.
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
                ServiceType.DUEL,
                this.hostName
        );
    }

    /**
     * Gets the configured hostname used for cluster identification.
     *
     * @return the hostname provided by the HOSTNAME environment variable, or `null` if not set
     */
    public String getHostName() {
        return hostName;
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