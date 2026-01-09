package club.revived.lobby.game.listener;

import club.revived.lobby.Lobby;
import club.revived.lobby.game.WarpLocation;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * PlayerListener
 *
 * @author yyuh
 * @since 09.01.26
 */
public final class PlayerListener implements Listener {

    /**
     * Creates and registers this listener with the server plugin manager so it begins receiving player interaction events.
     */
    public PlayerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    /**
     * Handles a player join and teleports the player to the server spawn
     *
     * @param event the player join event that triggered this handler
     */
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final var player = event.getPlayer();
        WarpLocation.SPAWN.teleport(player);
    }
}
