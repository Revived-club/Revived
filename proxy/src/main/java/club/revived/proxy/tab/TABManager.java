package club.revived.proxy.tab;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.proxy.ProxyPlugin;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TABManager {

    private final ProxyServer proxyServer = ProxyPlugin.getInstance().getServer();

    private final Map<UUID, TabListEntry> tabEntries = new ConcurrentHashMap<>();

    private static TABManager instance;

    private TABManager() {
        startEntryUpdateTask();
        startUpdateTask();

        instance = this;
    }

    public static TABManager getInstance() {
        if (instance == null) {
            new TABManager();
            return instance;
        }
        return instance;
    }

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
                                            .latency(-1)
                                            .profile(new GameProfile(
                                                    networkPlayer.getUuid(),
                                                    networkPlayer.getUsername(),
                                                    new ArrayList<>()
                                            ))
                                            .build());
                                });
                    }

                    final var uuids = PlayerManager.getInstance().getNetworkPlayers()
                            .values()
                            .stream().map(NetworkPlayer::getUuid)
                            .toList();

                    tabEntries.forEach((uuid, tabListEntry) -> {

                        for (final var entry : tabEntries.keySet()) {

                            if (uuids.contains(entry)) {
                                continue;
                            }

                            tabEntries.remove(entry);
                        }
                    });
                })
                .repeat(Duration.ofMillis(750L))
                .schedule();
    }

    public void startUpdateTask() {
        this.proxyServer.getScheduler().buildTask(ProxyPlugin.getInstance(), () -> {
                    final var players = this.proxyServer.getAllPlayers();

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
                                        .replace("<online>", String.valueOf(players.size()))
                                        .replace("<max-online>", String.valueOf(players.size() + 1))
                                        .replace("<ping>", String.valueOf(player.getPing()))
                                ));
                    }
                })
                .repeat(Duration.ofMillis(750L))
                .schedule();
    }

    public Map<UUID, TabListEntry> getTabEntries() {
        return tabEntries;
    }
}
