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

    /**
     * Create a tracker that waits for the specified player UUIDs to be online and invokes the provided callback
     * with all currently-online Player instances once every tracked player is present.
     *
     * The constructor will pre-populate the tracker with any players from {@code playerUUIDs} that are already
     * online; if any UUIDs remain to be observed it registers this instance as a listener for player join events,
     * otherwise it invokes {@code onAllOnline} immediately with the collected online players.
     *
     * @param plugin the plugin used to register event listeners
     * @param playerUUIDs the collection of player UUIDs to wait for
     * @param onAllOnline callback invoked with the list of online Player objects once all tracked players are online
     */
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

    /**
     * Creates a PlayerJoinTracker that waits for the given players to be online and invokes the callback when all are present.
     *
     * @param plugin the plugin used to register event listeners if needed
     * @param playerUUIDs the UUIDs of players to wait for
     * @param onAllOnline callback invoked with the list of online Player instances once all tracked players are online
     * @return a configured PlayerJoinTracker instance
     */
    public static PlayerJoinTracker of(
            final JavaPlugin plugin,
            final Collection<? extends UUID> playerUUIDs,
            final Consumer<List<Player>> onAllOnline
    ) {
        return new PlayerJoinTracker(plugin, playerUUIDs, onAllOnline);
    }

    /**
     * Handles a player join event, adding the player to the tracked online list if they were awaited.
     *
     * If this join satisfies the last awaited UUID, the listener is unregistered and the
     * `onAllOnline` callback is invoked with the list of all tracked online players.
     *
     * @param event the PlayerJoinEvent for the player who joined
     */
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

    /**
     * Adds the given player to the list of tracked online players.
     *
     * @param player the player to add
     */
    private void addOnlinePlayer(Player player) {
        onlinePlayers.add(player);
    }
}