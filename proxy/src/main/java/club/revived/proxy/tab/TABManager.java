package club.revived.proxy.tab;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.proxy.ProxyPlugin;
import club.revived.proxy.service.cluster.ServiceType;
import club.revived.proxy.service.player.NetworkPlayer;
import club.revived.proxy.service.player.PlayerManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.util.GameProfile;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TABManager {

    private final ProxyServer proxyServer = ProxyPlugin.getInstance().getServer();

    private final Map<UUID, TabListEntry> tabEntries = new ConcurrentHashMap<>();

    private static TABManager instance;

    /**
     * Initializes the TABManager singleton and begins periodic entry and display update tasks.
     * <p>
     * Sets this object as the singleton instance and starts the background tasks that
     * maintain per-player tab entries and refresh tab header/footer displays.
     */
    private TABManager() {
        startEntryUpdateTask();
        startUpdateTask();

        instance = this;
    }

    /**
     * Get the singleton TABManager instance, creating it if none exists.
     *
     * @return the singleton TABManager instance
     */
    public static TABManager getInstance() {
        if (instance == null) {
            new TABManager();
            return instance;
        }
        return instance;
    }

    /**
     * Starts a repeating task that synchronizes the manager's tabEntries with the current set of NetworkPlayer instances.
     * <p>
     * The task iterates online players to ensure a TabListEntry exists (using each player's TabList) for every NetworkPlayer,
     * and removes entries whose UUIDs are no longer present among NetworkPlayer instances. Runs repeatedly (every 750 ms).
     */
    public void startEntryUpdateTask() {
        this.proxyServer.getScheduler().buildTask(ProxyPlugin.getInstance(), () -> {
                    for (final Player player : proxyServer.getAllPlayers()) {

                        final var tabList = player.getTabList();
                        PlayerManager.getInstance().getNetworkPlayers()
                                .values()
                                .forEach(networkPlayer -> {
                                    tabEntries.computeIfAbsent(networkPlayer.getUuid(), u -> TabListEntry.builder()
                                            .tabList(tabList)
                                            .displayName(ColorUtils.parse(networkPlayer.getUsername()))
                                            .latency(20)
                                            .profile(new GameProfile(
                                                    networkPlayer.getUuid(),
                                                    networkPlayer.getUsername(),
                                                    List.of(new GameProfile.Property("textures", networkPlayer.getSkin(), networkPlayer.getSkinSignature()))
                                            ))
                                            .build());
                                });

                        tabEntries.forEach((_, tabListEntry) -> {
                            for (final var entry : tabEntries.keySet()) {

                                if (tabList.containsEntry(entry)) continue;

                                tabList.addEntry(tabListEntry);
                            }
                        });
                    }

                    final var uuids = PlayerManager.getInstance().getNetworkPlayers()
                            .values()
                            .stream().map(NetworkPlayer::getUuid)
                            .toList();

                    tabEntries.forEach((uuid, _) -> {

                        if (uuids.contains(uuid)) {
                            return;
                        }

                        tabEntries.remove(uuid);
                    });
                })
                .repeat(Duration.ofMillis(750L))
                .schedule();
    }

    /**
     * Schedules a recurring task that updates each online player's tab list header and footer.
     * <p>
     * The header shows a branded banner; the footer displays the player's current server name,
     * the network-wide online count, a simple max-online estimate (online + 1), and the player's ping.
     */
    public void startUpdateTask() {
        this.proxyServer.getScheduler().buildTask(ProxyPlugin.getInstance(), () -> {
                    final var players = this.proxyServer.getAllPlayers();
                    final var networkPlayers = PlayerManager.getInstance().getNetworkPlayers().size();

                    for (final Player player : players) {
                        final Optional<ServerConnection> serverOpt = player.getCurrentServer();
                        final String serverId = serverOpt.map(ServerConnection::getServer).map(RegisteredServer::getServerInfo).map(ServerInfo::getName).orElse("limbo");

                        player.sendPlayerListHeaderAndFooter(
                                ColorUtils.parse("""
                                        <#6aa2fc><strikethrough>                                            </strikethrough></#6aa2fc>
                                        
                                        <#3B82F6><bold>Revived.club</bold></#3B82F6>
                                        <white>Next-Generation PVP Practice
                                        """
                                ),
                                ColorUtils.parse("""
                                        
                                        <dark_gray>🞍● <white>Online: <#3B82F6><online>/<max-online> <dark_gray>🞍● <white>Ping<gray>: <green><ping>ms
                                        <dark_gray>→ <white>You are on <#3B82F6><server>
                                        <white>                                                        <white>
                                        <#6aa2fc><strikethrough>                                            </strikethrough></#6aa2fc>"""
                                        .replace("<server>", serverId)
                                        .replace("<online>", String.valueOf(networkPlayers))
                                        .replace("<max-online>", String.valueOf(networkPlayers + 1))
                                        .replace("<ping>", String.valueOf(player.getPing()))
                                ));
                    }
                })
                .repeat(Duration.ofMillis(750L))
                .schedule();
    }

    /**
     * Provides the current mapping of player UUIDs to their TabListEntry objects.
     *
     * @return the concurrent map from player UUID to TabListEntry (live reference)
     */
    public Map<UUID, TabListEntry> getTabEntries() {
        return tabEntries;
    }
}