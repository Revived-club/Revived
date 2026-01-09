package club.revived.lobby.game.billboard.listener;

import club.revived.lobby.Lobby;
import club.revived.lobby.game.billboard.BillboardManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class BillboardListener implements Listener {

    /**
     * Registers this BillboardListener with the server plugin manager so it receives Bukkit events for the Sandbox plugin.
     */
    public BillboardListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    /**
     * Adds the joining player as a viewer to every billboard's associated entities.
     *
     * @param event the player join event containing the joining player
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final var player = event.getPlayer();
        final var uuid = player.getUniqueId();

        BillboardManager.getInstance().getBillboards().forEach((key, entity) -> {
            for (final var wrapperEntity : entity.associated()) {
                wrapperEntity.addViewer(uuid);
            }
        });
    }

    /**
     * Handles a player quit event by removing the quitting player's UUID from every billboard wrapper entity's viewers.
     *
     * @param event the quit event whose player will be removed from all billboard viewers
     */
    @EventHandler
    public void onPlayerJoin(final PlayerQuitEvent event) {
        final var player = event.getPlayer();
        final var uuid = player.getUniqueId();

        BillboardManager.getInstance().getBillboards().forEach((key, entity) -> {
            for (final var wrapperEntity : entity.associated()) {
                wrapperEntity.removeViewer(uuid);
            }
        });
    }

}