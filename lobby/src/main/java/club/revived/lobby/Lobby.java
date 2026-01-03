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

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onDisable() {

    }

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

    public static Lobby getInstance() {
        return instance;
    }
}
