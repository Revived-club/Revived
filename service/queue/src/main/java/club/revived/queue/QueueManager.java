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

    /**
     * Initializes cluster integration and creates the game queue.
     *
     * <p>Configures the Redis-backed cluster and cache service using environment
     * variables and then instantiates the GameQueue.
     */
    public QueueManager() {
        this.setupCluster();
        new GameQueue();
    }

    /**
     * Initializes the application's Cluster using Redis configuration from environment variables.
     *
     * Reads REDIS_HOST and REDIS_PORT and configures a Redis-backed broker and cache, registering
     * the service under the name "queue-service".
     */
    private void setupCluster() {
        final String host = System.getenv("REDIS_HOST");
        final int port = Integer.parseInt(System.getenv("REDIS_PORT"));

        new Cluster(
                new RedisBroker(host, port, ""),
                new RedisCacheService(host, port, ""),
                "queue-service"
        );
    }
}