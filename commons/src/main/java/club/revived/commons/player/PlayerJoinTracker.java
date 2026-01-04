package club.revived.commons.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public final class PlayerJoinTracker implements Listener {

    private final Set<UUID> waitingFor;
    private final Consumer<List<Player>> onAllOnline;
    private final List<Player> onlinePlayers = new ArrayList<>();

    private PlayerJoinTracker(
            final JavaPlugin plugin,
            final Collection<? extends UUID> playerUUIDs,
            final Consumer<List<Player>> onAllOnline
    ) {
        this.waitingFor = new HashSet<>(playerUUIDs);
        this.onAllOnline = onAllOnline;

        for (final UUID uuid : playerUUIDs) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                addOnlinePlayer(player);
            }
        }

        if (!waitingFor.isEmpty()) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } else {
            onAllOnline.accept(onlinePlayers);
        }
    }

    public static PlayerJoinTracker of(
            final JavaPlugin plugin,
            final Collection<? extends UUID> playerUUIDs,
            final Consumer<List<Player>> onAllOnline
    ) {
        return new PlayerJoinTracker(plugin, playerUUIDs, onAllOnline);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (waitingFor.remove(event.getPlayer().getUniqueId())) {
            addOnlinePlayer(event.getPlayer());
            if (waitingFor.isEmpty()) {
                PlayerJoinEvent.getHandlerList().unregister(this);
                onAllOnline.accept(onlinePlayers);
            }
        }
    }

    private void addOnlinePlayer(Player player) {
        onlinePlayers.add(player);
    }
}