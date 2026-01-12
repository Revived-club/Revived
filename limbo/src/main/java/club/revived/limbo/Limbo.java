package club.revived.limbo;

import club.revived.limbo.service.broker.RedisBroker;
import club.revived.limbo.service.cache.RedisCacheService;
import club.revived.limbo.service.cluster.Cluster;
import club.revived.limbo.service.cluster.ServiceType;
import club.revived.limbo.service.status.ServiceStatus;
import com.loohp.limbo.plugins.LimboPlugin;

public final class Limbo extends LimboPlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        this.setupCluster();

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
     * Marks the cluster as shutting down when the plugin is disabled.
     *
     * <p>Sets Cluster.STATUS to ServiceStatus.SHUTTING_DOWN.</p>
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
                ServiceType.LIMBO,
                hostName
        );
    }
}