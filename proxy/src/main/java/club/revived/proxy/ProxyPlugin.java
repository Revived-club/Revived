package club.revived.proxy;

import club.revived.proxy.service.broker.RedisBroker;
import club.revived.proxy.service.cache.RedisCacheService;
import club.revived.proxy.service.cluster.Cluster;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.logging.Logger;

/**
 * ProxyPlugin
 *
 * @author yyuh
 * @since 04.01.26
 */
@Plugin(id = "revivedproxy", name = "revived-proxy", version = "1.0.0", authors = {"yyuh"})
public final class ProxyPlugin {

    private final ProxyServer server;
    private final Logger logger;

    private static ProxyPlugin instance;

    @Inject
    public ProxyPlugin(
            final ProxyServer server,
            final Logger logger
    ) {
        instance = this;

        this.server = server;
        this.logger = logger;

        this.setupCluster();
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
                hostName
        );
    }

    public static ProxyPlugin getInstance() {
        return instance;
    }

    public ProxyServer getServer() {
        return server;
    }
}
