package club.revived.lobby;

import club.revived.commons.inventories.impl.InventoryManager;
import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.WarpLocation;
import club.revived.lobby.game.billboard.BillboardManager;
import club.revived.lobby.game.billboard.command.BillboardCommand;
import club.revived.lobby.game.billboard.listener.BillboardListener;
import club.revived.lobby.game.billboard.listener.BillboardPacketListener;
import club.revived.lobby.game.chat.command.MessageCommand;
import club.revived.lobby.game.chat.command.ReplyCommand;
import club.revived.lobby.game.chat.listener.PlayerChatListener;
import club.revived.lobby.game.command.*;
import club.revived.lobby.game.duel.DuelManager;
import club.revived.lobby.game.item.ExecutableItemRegistry;
import club.revived.lobby.game.item.impl.LobbySelectorItem;
import club.revived.lobby.game.item.impl.MatchBrowserItem;
import club.revived.lobby.game.item.impl.PartyBrowserItem;
import club.revived.lobby.game.listener.ItemPlayerListener;
import club.revived.lobby.game.listener.PlayerListener;
import club.revived.lobby.game.listener.SpawnListener;
import club.revived.lobby.game.parties.PartyManager;
import club.revived.lobby.service.broker.RedisBroker;
import club.revived.lobby.service.cache.RedisCacheService;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.player.PlayerManager;
import club.revived.lobby.service.status.ServiceStatus;
import club.revived.lobby.util.HeadBuilder;
import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        final SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
        final APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .tickTickables()
                .usePlatformLogger();

        EntityLib.init(platform, settings);
    }

    /**
     * Initialize the plugin: set the singleton instance, register inventory and runtime components,
     * connect to the database, register commands and cluster services, and mark the cluster as available.
     */
    @Override
    public void onEnable() {
        instance = this;

        InventoryManager.register(this);
        this.createDataFolder();
        this.createDirs();
        this.connectDatabase();
        this.setupCluster();
        this.setLocations();

        new PlayerManager();
        new DuelManager();

        this.setupCommands();
        this.registerListeners();

        BillboardManager.getInstance().setup();
        ExecutableItemRegistry.register(
                new MatchBrowserItem(),
                new PartyBrowserItem(),
                new LobbySelectorItem()
        );

        Cluster.STATUS = ServiceStatus.AVAILABLE;
    }

    /**
     * Perform shutdown tasks for the plugin.
     * <p>
     * Updates the cluster status to ServiceStatus.SHUTTING_DOWN so other services are informed that this plugin is stopping.
     */
    @Override
    public void onDisable() {
        Cluster.STATUS = ServiceStatus.SHUTTING_DOWN;
    }

    /**
     * Initializes and refreshes all configured warp locations.
     */
    private void setLocations() {
        for (final var location : WarpLocation.values()) {
            location.update();
        }
    }

    /**
     * Initialize and register bukkit listeners
     */
    private void registerListeners() {
        new PlayerChatListener();
        new PlayerListener();
        new ItemPlayerListener();
        new BillboardListener();
        new BillboardPacketListener();
        new ReplyCommand();
        new SpawnListener();
    }

    /**
     * Initialize and register lobby command handlers.
     * <p>
     * Instantiates and registers the DuelCommand, WhereIsCommand, PingCommand, and QueueCommand handlers.
     */
    private void setupCommands() {
        new DuelCommand();
        new AdminCommand();
        new WhereIsCommand();
        new PingCommand();
        new QueueCommand();
        new BillboardCommand();
        new ArenaCommand();
        new WhereIsProxyCommand();
        new PartyCommand();
        new MessageCommand();
    }

    private void createDataFolder() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
    }

    private void createDirs() {
        List.of(
                "schem"
        ).forEach(s -> {
            final var file = new File(this.getDataFolder(), s);

            if (!file.exists()) {
                file.mkdirs();
            }
        });
    }


    /**
     * Initializes a MongoDB connection from environment-provided credentials and registers it with the DatabaseManager.
     * <p>
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
     * Configures and instantiates the plugin Cluster from environment variables.
     * <p>
     * Reads the following environment variables and uses them to create a Cluster:
     * HOSTNAME, REDIS_HOST, REDIS_PORT (REDIS_PORT is parsed as a base-10 integer).
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