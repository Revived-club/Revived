package club.revived.lobby.game.listener;

import club.revived.lobby.Lobby;
import club.revived.lobby.game.WarpLocation;
import club.revived.lobby.game.item.ExecutableItemRegistry;
import club.revived.lobby.game.item.ExecutableItemType;
import club.revived.lobby.game.player.PlayerProfileManager;
import club.revived.lobby.util.SkinUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

        player.getInventory().clear();

        player.setGameMode(GameMode.ADVENTURE);
        event.joinMessage(null);

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                -1,
                1,
                true
        ));

        final var item = ExecutableItemRegistry.byType(ExecutableItemType.MATCH_BROWSER).toBukkitItem();
        player.getInventory().setItem(0, item);

        final var partyBrowser = ExecutableItemRegistry.byType(ExecutableItemType.PARTY_BROWSER).toBukkitItem();
        player.getInventory().setItem(1, partyBrowser);

        final var lobbySelector = ExecutableItemRegistry.byType(ExecutableItemType.LOBBY_SELECTOR).toBukkitItem();
        player.getInventory().setItem(8, lobbySelector);

        PlayerProfileManager.getInstance().update(
                player.getUniqueId(),
                player.getName(),
                SkinUtils.getSkin(player),
                System.currentTimeMillis()
        );
    }
}
