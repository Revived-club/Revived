package club.revived.lobby.game.listener;

import club.revived.lobby.Lobby;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.messaging.impl.BroadcastMessage;
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
     * Creates a PlayerChatListener and registers it with the server plugin manager using the Lobby plugin instance.
     */
    public PlayerChatListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    /**
     * Handles player chat events by broadcasting a formatted global chat message and preventing default chat processing.
     *
     * <p>If the incoming {@code AsyncChatEvent} is not already cancelled, this handler cancels the event, formats the
     * player's name and serialized chat content into a colored message, and sends that message as a global broadcast
     * via the cluster messaging service.</p>
     *
     * @param event the async chat event containing the player's message; this event will be cancelled to suppress default chat handling
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