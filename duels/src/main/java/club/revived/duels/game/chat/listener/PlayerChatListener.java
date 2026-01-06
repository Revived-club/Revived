package club.revived.duels.game.chat.listener;

import club.revived.duels.Duels;
import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.messaging.impl.BroadcastMessage;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * PlayerChatListener
 *
 * @author yyuh
 * @since 06.01.26
 */
public final class PlayerChatListener implements Listener {

    /**
     * Creates a PlayerChatListener and registers it with the server plugin manager.
     *
     * Registers this listener instance so it receives Bukkit events using the Duels plugin instance.
     */
    public PlayerChatListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Duels.getInstance());
    }

    /**
     * Handles player async chat events by cancelling the default chat and broadcasting a formatted global message containing the player's name and serialized chat content.
     *
     * If the event is already cancelled, no action is taken.
     *
     * @param event the AsyncChatEvent to process
     */
    @EventHandler
    public void onChat(final AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final var player = event.getPlayer();

        event.setCancelled(true);

        final var messageStr = String.format("<white>%s <dark_gray>»</dark_gray> %s",
                player.getName(),
                MiniMessage.miniMessage().serialize(event.message())
        );

        Cluster.getInstance().getMessagingService().sendGlobalMessage(new BroadcastMessage(messageStr));
    }
}