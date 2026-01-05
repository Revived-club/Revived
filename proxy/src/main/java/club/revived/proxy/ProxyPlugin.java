package club.revived.proxy;

import club.revived.proxy.service.broker.RedisBroker;
import club.revived.proxy.service.cache.RedisCacheService;
import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.player.PlayerManager;
import club.revived.proxy.service.status.ServiceStatus;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
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

    /**
     * Creates the plugin instance, stores the injected server and logger, and initializes cluster support.
     *
     * <p>Sets the static singleton reference to this instance, retains the provided {@code ProxyServer}
     * and {@code Logger}, and invokes cluster setup. </p>
     */
    @Inject
    public ProxyPlugin(
            final ProxyServer server,
            final Logger logger
    ) {
        instance = this;
        this.server = server;
        this.logger = logger;

        System.out.println("Loading Plugin...");
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        System.out.println("Initializing Plugin...");
        this.setupCluster();
        new PlayerManager();

        Cluster.STATUS = ServiceStatus.AVAILABLE;
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

    /**
     * Retrieve the singleton ProxyPlugin instance.
     *
     * @return the singleton ProxyPlugin instance, or null if the plugin has not been initialized
     */
    public static ProxyPlugin getInstance() {
        return instance;
    }

    /**
     * Retrieve the plugin's ProxyServer instance.
     *
     * @return the ProxyServer used by this plugin
     */
    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }
}