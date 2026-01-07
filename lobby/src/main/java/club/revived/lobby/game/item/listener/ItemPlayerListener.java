package club.revived.lobby.game.item.listener;

import club.revived.lobby.Lobby;
import club.revived.lobby.game.item.ExecutableItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * ItemPlayerListener
 *
 * @author yyuh - DL
 * @since 1/7/26
 */
public final class ItemPlayerListener implements Listener {

    public ItemPlayerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final var player = event.getPlayer();
        final var handItem = player.getInventory().getItemInMainHand();

        if (ExecutableItemRegistry.isExecutable(handItem)) {
            ExecutableItemRegistry.execute(player, handItem);
        }
    }
}
