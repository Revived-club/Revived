package club.revived.lobby.game.chat.listener;

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

    public PlayerChatListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Lobby.getInstance());
    }

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
