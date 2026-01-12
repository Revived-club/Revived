package club.revived.proxy.listener;

import club.revived.proxy.ProxyPlugin;
import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.cluster.ClusterService;
import club.revived.proxy.service.cluster.ServiceType;
import club.revived.proxy.service.messaging.impl.QuitNetwork;
import club.revived.proxy.service.player.PlayerManager;
import club.revived.proxy.service.status.ServiceStatus;
import club.revived.proxy.service.status.StatusRequest;
import club.revived.proxy.service.status.StatusResponse;
import club.revived.proxy.tab.TABManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class PlayerListener {

    @Subscribe
    public void onServerConnect(final PlayerChooseInitialServerEvent event) {
        final Player player = event.getPlayer();

        event.setInitialServer(null);

        findAvailableLobbyServer(player, 0);
    }

    /**
     * Recursively attempts to find and connect a player to an available lobby server.
     *
     * @param player  the player attempting to connect
     * @param attempt the current attempt number (for retry limiting)
     */
    private void findAvailableLobbyServer(final Player player, final int attempt) {
        final int MAX_ATTEMPTS = 3;

        if (attempt >= MAX_ATTEMPTS) {
            player.disconnect(Component.text("Failed to find an available lobby server after " + MAX_ATTEMPTS + " attempts.")
                    .style(style -> style.color(NamedTextColor.RED)));
            return;
        }

        final List<ClusterService> lobbyServers = Cluster.getInstance().getServices().values()
                .stream()
                .filter(service -> service.getType().equals(ServiceType.LOBBY))
                .sorted(Comparator.comparingInt(service -> service.getOnlinePlayers().size()))
                .toList();

        if (lobbyServers.isEmpty()) {
            player.disconnect(Component.text("There are no available lobby servers!")
                    .style(style -> style.color(NamedTextColor.RED)));
            return;
        }

        tryConnectToServers(player, lobbyServers, 0, attempt);
    }

    /**
     * Attempts to connect the player to servers from the list, checking availability first.
     *
     * @param player        the player to connect
     * @param servers       the list of potential lobby servers
     * @param serverIndex   the current server index in the list
     * @param globalAttempt the global retry attempt number
     */
    private void tryConnectToServers(
            final Player player,
            final List<ClusterService> servers,
            final int serverIndex,
            final int globalAttempt
    ) {
        if (serverIndex >= servers.size()) {
            System.out.println("[ServerConnect] All servers unavailable for " + player.getUsername() +
                    ", retrying (attempt " + (globalAttempt + 1) + ")...");

            ProxyPlugin.getInstance().getServer()
                    .getScheduler()
                    .buildTask(ProxyPlugin.getInstance(), () -> findAvailableLobbyServer(player, globalAttempt + 1))
                    .delay(500, TimeUnit.MILLISECONDS)
                    .schedule();
            return;
        }

        final ClusterService selectedServer = servers.get(serverIndex);
        final RegisteredServer server = ProxyPlugin.getInstance().getServer()
                .getServer(selectedServer.getId())
                .orElse(null);

        if (server == null) {
            System.out.println("[ServerConnect] Server " + selectedServer.getId() + " not registered, trying next...");
            tryConnectToServers(player, servers, serverIndex + 1, globalAttempt);
            return;
        }

        Cluster.getInstance().getMessagingService()
                .sendRequest(selectedServer.getId(), new StatusRequest(), StatusResponse.class)
                .thenAccept(statusResponse -> {
                    if (statusResponse == null || statusResponse.status() != ServiceStatus.AVAILABLE) {
                        System.out.println("[ServerConnect] Server " + selectedServer.getId() +
                                " reported unavailable, trying next...");
                        tryConnectToServers(player, servers, serverIndex + 1, globalAttempt);
                        return;
                    }

                    System.out.println("[ServerConnect] Connecting " + player.getUsername() +
                            " to " + selectedServer.getId());

                    player.createConnectionRequest(server).connect()
                            .thenAccept(result -> {
                                if (!result.isSuccessful()) {
                                    System.out.println("[ServerConnect] Connection to " + selectedServer.getId() +
                                            " failed: " + result.getReasonComponent().orElse(Component.empty()));
                                    tryConnectToServers(player, servers, serverIndex + 1, globalAttempt);
                                } else {
                                    System.out.println("[ServerConnect] Successfully connected " + player.getUsername() +
                                            " to " + selectedServer.getId());
                                }
                            })
                            .exceptionally(throwable -> {
                                System.err.println("[ServerConnect] Exception connecting to " + selectedServer.getId() +
                                        ": " + throwable.getMessage());
                                tryConnectToServers(player, servers, serverIndex + 1, globalAttempt);
                                return null;
                            });
                })
                .exceptionally(throwable -> {
                    System.err.println("[ServerConnect] Exception requesting status from " + selectedServer.getId() +
                            ": " + throwable.getMessage());
                    tryConnectToServers(player, servers, serverIndex + 1, globalAttempt);
                    return null;
                });
    }

    /**
     * Removes the disconnecting player's tab entry from TABManager.
     *
     * @param event the disconnect event containing the player that is leaving
     */
    @Subscribe
    public void onQuit(final @NotNull DisconnectEvent event) {
        final Player player = event.getPlayer();

        TABManager.getInstance().getTabEntries().remove(player.getUniqueId());

        Cluster.getInstance().getMessagingService().sendGlobalMessage(new QuitNetwork(player.getUniqueId()));
    }

    /**
     * Update the server list ping to reflect the current network player counts.
     * <p>
     * Sets the ping's onlinePlayers to the current network size and maximumPlayers to one greater.
     *
     * @param event the ProxyPingEvent whose ServerPing will be modified
     */
    @Subscribe
    public void onPing(final ProxyPingEvent event) {
        final var networkPlayers = PlayerManager.getInstance().getNetworkPlayers().size();
        final ServerPing.Builder pingBuilder = event.getPing().asBuilder();

        pingBuilder.onlinePlayers(networkPlayers);
        pingBuilder.maximumPlayers(networkPlayers + 1);
        event.setPing(pingBuilder.build());
    }
}