package club.revived.queue;

import club.revived.queue.cluster.broker.RedisBroker;
import club.revived.queue.cluster.cache.RedisCacheService;
import club.revived.queue.cluster.cluster.Cluster;

/**
 * QueueManager
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public final class QueueManager {

    public QueueManager() {
        this.setupCluster();
        new GameQueue();
    }

    /**
     * Initializes the application's Cluster integration using Redis and environment configuration.
     * <p></p>
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
                hostName
        );
    }
}
