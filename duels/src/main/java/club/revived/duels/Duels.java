package club.revived.duels;

import club.revived.commons.inventories.impl.InventoryManager;
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


    /**
     * Initializes the plugin: sets the singleton instance, reads the HOSTNAME environment variable into {@code hostName}, and configures the cluster.
     *
     * @throws RuntimeException if retrieving the HOSTNAME environment variable fails
     */
    @Override
    public void onEnable() {
        instance = this;

        InventoryManager.register(this);

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
     * Accesses the singleton Duels instance.
     *
     * @return the shared Duels instance, or null if the plugin has not been enabled.
     */
    public static Duels getInstance() {
        return instance;
    }
}