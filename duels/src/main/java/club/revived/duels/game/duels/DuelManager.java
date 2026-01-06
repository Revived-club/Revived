package club.revived.duels.game.duels;

import club.revived.commons.player.PlayerJoinTracker;
import club.revived.duels.Duels;
import club.revived.duels.game.arena.pooling.ArenaPoolManager;
import club.revived.duels.game.kit.EditedDuelKit;
import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.messaging.impl.DuelStart;
import club.revived.duels.service.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * DuelManager
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class DuelManager {

    private final Map<UUID, Duel> runningDuels = new HashMap<>();

    /**
     * Initializes a DuelManager and registers a handler to start duels when a DuelStart message is received.
     *
     * <p>As a side effect, a message handler for DuelStart events is registered with the cluster messaging service.</p>
     */
    public DuelManager() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(DuelStart.class, this::startDuel);
    }

    /**
     * Initiates a duel from a DuelStart message: reserves an arena, creates and registers the Duel,
     * prepares and teleports participants, loads their kits, pushes the duel to the global cache,
     * and starts the duel countdown task.
     *
     * @param duelStart message containing the blue and red team UUID lists, the number of rounds,
     *                  and the kit type used to configure the duel
     */
    private void startDuel(final DuelStart duelStart) {
        final List<UUID> blueTeam = duelStart.blueTeam();
        final List<UUID> redTeam = duelStart.redTeam();
        final int rounds = duelStart.rounds();
        final KitType kitType = duelStart.kitType();

        ArenaPoolManager.getInstance().getArena(kitType).thenAccept(arena -> {
            final var duel = new Duel(
                    blueTeam,
                    redTeam,
                    rounds,
                    kitType
            );

            // TODO: Replace? Cant think of a better solutation rn :3 :§ :§ § :3
            final var uuids = new HashSet<>(redTeam);
            uuids.addAll(blueTeam);

            final var networkPlayers = uuids.stream()
                    .map(uuid -> PlayerManager.getInstance().fromBukkitPlayer(uuid))
                    .toList();

            networkPlayers.forEach(networkPlayer -> {
                networkPlayer.connect(Cluster.getInstance().getServiceId());
                this.runningDuels.put(networkPlayer.getUuid(), duel);
            });

            Cluster.getInstance().getGlobalCache().push("games", duel);

            PlayerJoinTracker.of(Duels.getInstance(), uuids, players -> {
                for (final var player : players) {
                    player.sendRichMessage("""
                            
                            <#3B82F6><bold>Duel Info<reset>
                            <white>First To: <#3B82F6><to>
                            <white>Duel Kit: <#3B82F6><kit>
                            <white>Players: <#3B82F6><players>
                            
                            """
                            .replace("<kit>", kitType.getBeautifiedName())
                            .replace("<to>", String.valueOf(rounds))
                            .replace("<players>", String.join(", ", players
                                    .stream()
                                    .map(Player::getName)
                                    .toArray(String[]::new)))
                    );

                    this.healPlayer(player);

                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                    networkPlayer.getCachedOrLoad(EditedDuelKit.class).thenAccept(editedDuelKit -> {
                        player.getInventory().setContents(editedDuelKit.content().values().toArray(new ItemStack[0]));
                    });

                    this.runningDuels.put(player.getUniqueId(), duel);
                }

                for (final Player redPlayer : duel.getRedPlayers()) {
                    redPlayer.teleportAsync(arena.getSpawn1().add(0, 1, 0));
                }

                for (final Player bluePlayer : duel.getBluePlayers()) {
                    bluePlayer.teleportAsync(arena.getSpawn2().add(0, 1, 0));
                }

                new DuelStartTask(3, duel);
            });
        });
    }

    /**
     * Restore a player's health, hunger, exhaustion, potion effects, and extinguish fire.
     *
     * The restoration is performed on the server's main thread.
     *
     * @param player the Bukkit player to restore
     */
    private void healPlayer(final Player player) {
        Bukkit.getScheduler().runTask(Duels.getInstance(), () -> {
            player.heal(20.0);
            player.setExhaustion(0);
            player.setFoodLevel(20);
            player.clearActivePotionEffects();
            player.setFireTicks(0);
        });
    }

    /**
     * Retrieve the registry of active duels keyed by participant UUID.
     *
     * The returned map associates each participant's UUID with the Duel they are currently in.
     * This is the live internal registry and may be modified by callers.
     *
     * @return the map from player UUID to their active Duel
     */
    public Map<UUID, Duel> getRunningDuels() {
        return runningDuels;
    }
}