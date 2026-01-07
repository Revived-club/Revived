package club.revived.proxy.listener;

import club.revived.proxy.ProxyPlugin;
import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.cluster.ClusterService;
import club.revived.proxy.service.cluster.ServiceType;
import club.revived.proxy.service.player.PlayerManager;
import club.revived.proxy.tab.TABManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public final class PlayerListener {

    /**
     * Chooses an initial lobby server for a joining player and assigns it to the event.
     * <p></p>
     * If one or more lobby servers are available, the least-populated lobby server is selected and set as the player's initial server.
     * If no lobby server can be resolved or the selected server is not registered with the proxy, the player is disconnected with an error message.
     *
     * @param event the server-selection event for the joining player
     */
    @Subscribe
    public void onServerConnect(final PlayerChooseInitialServerEvent event) {
        final Player player = event.getPlayer();

        final List<ClusterService> lobbyServers = Cluster.getInstance().getServices().values()
                .stream()
                .filter(onlineServer -> onlineServer.getType().equals(ServiceType.LOBBY))
                .sorted(Comparator.comparingInt(service -> service.getOnlinePlayers().size()))
                .toList();

        if (lobbyServers.isEmpty()) {
            player.disconnect(Component.text("There are no available lobby servers!").style(style -> style.color(NamedTextColor.RED)));
            return;
        }

        final var selectedServer = lobbyServers.getFirst();
        if (selectedServer == null) {
            player.disconnect(Component.text("Could not find lobby server!").style(style -> style.color(NamedTextColor.RED)));
            return;
        }

        final RegisteredServer server = ProxyPlugin.getInstance().getServer().getServer(selectedServer.getId()).orElse(null);

        if (server == null) {
            player.disconnect(Component.text("Could not connect to " + selectedServer.getId()).style(style -> style.color(NamedTextColor.RED)));
            return;
        }

        event.setInitialServer(server);
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
    }

    /**
     * Update the server list ping to reflect the current network player counts.
     *
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