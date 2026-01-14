package club.revived.duels.game.duels.listener;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.location.BukkitCuboidRegion;
import club.revived.duels.Duels;
import club.revived.duels.game.arena.ArenaType;
import club.revived.duels.game.arena.IArena;
import club.revived.duels.game.arena.impl.DuelArena;
import club.revived.duels.game.duels.*;
import club.revived.duels.game.duels.ffa.FFA;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PlayerListener implements Listener {

    private final Duels instance = Duels.getInstance();
    private final DuelManager duelManager = DuelManager.getInstance();

    private final static List<KitType> saturatedKits = List.of(KitType.AXE, KitType.SWORD, KitType.SMP, KitType.NETHERITE_POTION, KitType.DIAMOND_POTION, KitType.MACE, KitType.SPLEEF);

    /**
     * Creates a PlayerListener and registers it with the Bukkit plugin manager so it receives Duels-related events.
     */
    public PlayerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Duels.getInstance());
    }

    /**
     * Prevents hunger level changes for players participating in duels that use kits requiring saturated hunger.
     * <p></p>
     * If the event's entity is a player who is currently in a duel and the duel's kit type is listed in
     * {@code saturatedKits}, the event is cancelled to stop hunger changes.
     *
     * @param event the food level change event to evaluate and possibly cancel
     */
    @EventHandler
    public void onHunger(final FoodLevelChangeEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof final Player player) {
            if (!duelManager.isDueling(player)) return;

            final Game duel = duelManager.getDuel(player);

            if (duel == null) {
                return;
            }

            if (saturatedKits.contains(duel.getKitType())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Prevents any item drops from explosions by setting the explosion yield to zero.
     *
     * @param event the explosion event whose yield is cleared
     */
    @EventHandler
    public void handle(final EntityExplodeEvent event) {
        event.setYield(0);
    }

    /**
     * Prevents any item drops from block explosions by setting the explosion yield to zero.
     *
     * @param event the BlockExplodeEvent whose block drop yield will be set to 0
     */
    @EventHandler
    public void handle(final BlockExplodeEvent event) {
        event.setYield(0);
    }

    /**
     * Prevents falling-block entities from converting into placed blocks.
     * <p></p>
     * Cancels the provided event when the changing entity is a falling block,
     * stopping the falling-block from becoming a block in the world.
     *
     * @param event the block-change event to inspect and potentially cancel
     */
    @EventHandler
    public void handle(final EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents a player from switching their spectating target to anyone other than the recorded target.
     *
     * @param event the spectating start event; cancelled when the new spectator target does not match the recorded target for that player
     */
//    @EventHandler
//    public void onSpectatingStart(PlayerStartSpectatingEntityEvent event) {
//        final Player player = event.getPlayer();
//        if (event.getNewSpectatorTarget() instanceof Player target && DuelManager.getInstance().getSpectating().containsKey(player)) {
//            if (!DuelManager.getInstance().getSpectating().get(player).equals(target)) {
//                event.setCancelled(true);
//            }
//        }
//    }

    /**
     * Prevents players from damaging teammates during duels.
     * <p></p>
     * If both the damaged entity and the damager are players participating in a duel,
     * and the attacker is on the same team as the victim, the damage event is cancelled
     * and the attacker is notified.
     *
     * @param event the damage event to inspect and possibly cancel
     */
    @EventHandler
    public void onAttack(final @NotNull EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!duelManager.isDueling(victim) && !duelManager.isDueling(attacker)) {
            return;
        }

        final Game game = duelManager.getDuel(victim);

        if (game == null) {
            return;
        }

        if (game instanceof final Duel duel) {
            final var victimTeam = duel.getTeam(victim);

            if (victimTeam.hasPlayer(attacker)) {
                attacker.sendRichMessage("<red>You can't attack your Team Mates!");
                event.setCancelled(true);
            }
        }
    }

    /**
     * Resolve duel state when a player disconnects and suppress the quit message.
     *
     * <p>If the player is participating in an active duel, determines the player's team. If that team is fully
     * eliminated, awards a point to the opposing team, marks the duel as ending, displays match results, and ends
     * the duel. Otherwise, notifies remaining duel participants that the player quit and was removed.</p>
     *
     * @param event the PlayerQuitEvent for the disconnecting player
     */
    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        event.quitMessage(null);

        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);

        if (game == null || game.getGameState() == GameState.ENDING) return;

        if (game instanceof final Duel duel) {
            final var playerTeam = duel.getTeam(player);
            final var winnerTeam = duel.getOpposing(playerTeam);

            if (isWholeTeamDead(playerTeam)) {
                winnerTeam.addScore(1);
                duel.setGameState(GameState.ENDING);

                showResults(playerTeam, winnerTeam);

                duelManager.endDuel(
                        duel,
                        winnerTeam,
                        playerTeam
                );

            } else {
                for (Player duelPlayer : duel.getPlayers()) {
                    duelPlayer.sendRichMessage("<gray>\uD83D\uDDE1 <gray><player> quit the game and got kicked from the duel.".replace("<player>", player.getName()));
                }
            }
        } else if (game instanceof final FFA ffa) {
            for (final Player ffaPlayer : ffa.getPlayers()) {
                ffaPlayer.sendRichMessage("<gray>\uD83D\uDDE1 <gray><player> quit the game and got kicked from the FFA.".replace("<player>", player.getName()));
            }

            final List<Player> survivors = ffa.getPlayers().stream()
                    .filter(p -> p.getGameMode() != GameMode.SPECTATOR && !p.getUniqueId().equals(player.getUniqueId()))
                    .toList();

            if (survivors.size() <= 1) {
                final Player winner = survivors.isEmpty() ? player : survivors.get(0);
                duelManager.endFFA(ffa, winner);
            }
        }


    }

    /**
     * Consumes a custom "golden head" item when the player right-clicks it, grants short-term combat buffs, and applies a cooldown.
     * <p></p>
     * If the held item has the persistent data key `_uhc` with value `"golden_head"`, the interaction is cancelled, one item is
     * consumed (if not on cooldown), a 30-second cooldown is applied to that item, and the player receives absorption (120s, amp 1),
     * regeneration (20s, amp 1), and speed (10s, amp 1) potion effects. Sends a confirmation message to the player.
     *
     * @param event the PlayerInteractEvent that triggered the interaction
     */
    @EventHandler
    public void onGoldenHead(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem.isEmpty() || handItem.getType().isAir()) return;

        String handItemPDC = handItem.getPersistentDataContainer().get(new NamespacedKey("azareth", "_uhc"), PersistentDataType.STRING);

        if (handItemPDC == null) return;
        if (!event.getAction().isRightClick()) return;

        if (handItemPDC.equals("golden_head")) {
            event.setCancelled(true);

            if (!player.hasCooldown(handItem)) {
                handItem.setAmount(handItem.getAmount() - 1);
                player.setCooldown(handItem, 20 * 30);

                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1));

                player.sendRichMessage("<gold>You consumed a golden head");
            }
        }
    }

    /**
     * Handle a player's death during an active duel: suppress the death message, set the player to
     * spectator, notify all duel participants of the death, and resolve the round or match if the
     * player's team has been fully eliminated.
     *
     * <p>If the victim's team is completely eliminated this method awards a point to the opposing
     * team, transitions the duel to the ENDING state, displays end-of-match titles to participants,
     * and either ends the duel (if the match is over) or schedules the next round shortly thereafter.
     */
    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {
        final Player player = event.getPlayer();

        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);
        if (game == null || game.getGameState() != GameState.RUNNING) return;

        event.deathMessage(null);
        event.setCancelled(true);

        player.setGameMode(GameMode.SPECTATOR);

        if (game instanceof final Duel duel) {
            final DuelTeam victimTeam = duel.getTeam(player);

            for (final Player duelPlayer : duel.getPlayers()) {
                duelPlayer.sendRichMessage("<gray>\uD83D\uDDE1 <victim> died"
                        .replace("<victim>", player.getName()));
            }

            if (!isWholeTeamDead(victimTeam)) return;

            final DuelTeam winnerTeam = duel.getOpposing(victimTeam);
            winnerTeam.addScore(1);
            duel.setGameState(GameState.ENDING);

            showResults(victimTeam, winnerTeam);

            if (duel.isOver()) {
                duelManager.endDuel(duel, winnerTeam, victimTeam);
            } else {
                Bukkit.getScheduler().runTaskLater(instance, () -> duelManager.startNewRound(duel), 40L);
            }
        } else if (game instanceof final FFA ffa) {
            for (final Player ffaPlayer : ffa.getPlayers()) {
                ffaPlayer.sendRichMessage("<gray>\uD83D\uDDE1 <victim> died"
                        .replace("<victim>", player.getName()));
            }

            final List<Player> survivors = ffa.getPlayers().stream()
                    .filter(p -> p.getGameMode() != GameMode.SPECTATOR)
                    .toList();

            if (survivors.size() <= 1) {
                final Player winner = survivors.isEmpty() ? player : survivors.get(0);
                duelManager.endFFA(ffa, winner);
            }
        }
    }

    /**
     * Suppresses the in-game advancement message when a player completes an advancement.
     *
     * @param event the advancement completion event whose broadcast message will be cleared
     */
    @EventHandler
    public void onAchievement(final PlayerAdvancementDoneEvent event) {
        event.message(null);
    }

    /**
     * Handles player join events by suppressing the join message and ensuring a dead player is respawned.
     *
     * @param event the join event; its join message will be cleared and the joining player will be respawned if they are dead
     */
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        event.joinMessage(null);

        final Player player = event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);

        if (player.isDead()) {
            player.spigot().respawn();
        }
    }

    /**
     * Removes snow blocks created by snowball impacts.
     * <p></p>
     * If a snowball lands on a block that is a `SNOW_BLOCK`, that block is removed.
     *
     * @param event the projectile hit event containing the projectile and the hit block
     */
    @EventHandler
    public void onProjectileLand(final ProjectileHitEvent event) {
        if (event.getEntity().getType() != EntityType.SNOWBALL) return;
        if (event.getHitBlock() == null) return;

        final Block block = event.getHitBlock();
        if (block.getType() == Material.SNOW_BLOCK) {
            block.setType(Material.AIR);
        }
    }

    /**
     * Handles damage events for players in running SPLEEF duels, applying snowball-specific effects and preventing other damage.
     *
     * <p>If the damaged entity is a player participating in a duel whose state is RUNNING and whose kit is SPLEEF,
     * this handler sets damage to 0.1 and restores the player's health to 20.0 when the damager is a snowball;
     * for any other damager the event is cancelled.</p>
     */
    @EventHandler
    public void onEntityHit(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;
        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);
        if (game == null || game.getGameState() != GameState.RUNNING) return;

        if (game.getKitType() != KitType.SPLEEF) return;

        if (event.getDamager() instanceof org.bukkit.entity.Snowball) {
            event.setDamage(0.1);
            player.setHealth(20.0);
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents fall damage for players in SPLEEF duels and cancels all damage for players when their duel is not in the RUNNING state.
     *
     * <p>If the event's entity is a player participating in a duel that is not RUNNING, the event is cancelled.
     * If the duel is RUNNING and the damage cause is FALL while the duel's kit is SPLEEF, the fall damage is cancelled.</p>
     *
     * @param event the entity damage event to evaluate and possibly cancel
     */
    @EventHandler
    public void onFallDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;
        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);
        if (game == null) return;

        if (game.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && game.getKitType() == KitType.SPLEEF) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels a player's interaction if they are in a duel that is not in the RUNNING state.
     * <p></p>
     * If the player is not dueling or the duel cannot be resolved, the interaction is not modified.
     *
     * @param event the player interaction event
     */
    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);
        if (game == null) return;

        if (game.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels damage for players who are currently in a duel that is not in the RUNNING state.
     * <p></p>
     * If the damaged entity is a player participating in a duel and that duel's state is not RUNNING,
     * the event is cancelled to prevent any damage from applying.
     *
     * @param event the damage event to evaluate and potentially cancel
     */
    @EventHandler
    public void onGenericDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);
        if (game == null) return;

        if (game.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents players from dropping items while they are in a duel that is not running.
     * <p></p>
     * Cancels the provided PlayerDropItemEvent when the event's player is currently in a duel whose game state is not RUNNING.
     *
     * @param event the drop event to evaluate and potentially cancel
     */
    @EventHandler
    public void onDrop(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);
        if (game == null) return;

        if (game.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels an item pickup when the player's active duel is ending.
     *
     * @param event the player-attempt-pickup-item event; cancelled if the player's duel is in the ENDING state
     */
    @EventHandler
    public void onPickUp(final PlayerAttemptPickupItemEvent event) {
        final Player player = event.getPlayer();
        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);
        if (game == null) return;

        if (game.getGameState() == GameState.ENDING) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles block placement during duels, restricting placement based on duel state and arena type and recording placed blocks for non-interactive duel arenas.
     * <p></p>
     * If the player is not in a duel this handler does nothing. Placement is cancelled when the duel is STARTING or ENDING. Placement is allowed without tracking in arenas of type INTERACTIVE. For arenas that are instances of DuelArena, the placed block's location is added to the arena's modifiedLocations set for later restoration or tracking.
     *
     * @param event the BlockPlaceEvent representing the attempted block placement
     */
    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final var player = event.getPlayer();

        if (!duelManager.isDueling(player)) return;

        final var game = duelManager.getDuel(player);

        if (game == null) {
            return;
        }

        if (game.getGameState() == GameState.ENDING ||
                game.getGameState() == GameState.STARTING) {
            event.setCancelled(true);
            return;
        }

        final IArena arena = game.getArena();

        if (arena.getArenaType() == ArenaType.INTERACTIVE) {
            return;
        }


        if (arena instanceof final DuelArena duelArena) {
            final Location location = event.getBlock().getLocation();
            duelArena.getModifiedLocations().add(location);
        }
    }

    /**
     * Enforces duel-specific block-breaking rules by cancelling unauthorized breaks inside duels.
     * <p></p>
     * Cancels the event when the breaker is participating in a duel that is STARTING or ENDING,
     * or when the arena is a DuelArena and the broken block's location is not recorded as a modified location.
     *
     * @param event the BlockBreakEvent to evaluate and possibly cancel
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();

        if (!duelManager.isDueling(player)) return;

        final Game game = duelManager.getDuel(player);

        if (game == null) {
            return;
        }

        if (game.getGameState() == GameState.ENDING ||
                game.getGameState() == GameState.STARTING) {
            event.setCancelled(true);
            return;
        }

        final IArena arena = game.getArena();

        if (arena instanceof final DuelArena duelArena) {
            final Location location = event.getBlock().getLocation();

            if (!duelArena.getModifiedLocations().contains(location)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Registers the destination location as a modified arena block when a block flow results in certain solid block types.
     * <p></p>
     * Evaluates the event's destination block and, if its resulting material is COBBLESTONE, STONE, OBSIDIAN, or BASALT,
     * records the block location so arena modifications can be tracked/restored.
     *
     * @param event the block-from-to event describing a block changing position/state due to fluid flow
     */
    @EventHandler
    public void onBlockFromTo(final BlockFromToEvent event) {
        final Block block = event.getToBlock();
        final Material newType = block.getType();

        if (newType == Material.COBBLESTONE || newType == Material.STONE || newType == Material.OBSIDIAN || newType == Material.BASALT) {
            trackModifiedBlockInArena(block.getLocation());
        }
    }

    /**
     * Registers the location of a burned block as a modified block in the containing active duel arena.
     *
     * @param event the BlockBurnEvent whose block location should be tracked
     */
    @EventHandler
    public void onBlockBurn(final BlockBurnEvent event) {
        trackModifiedBlockInArena(event.getBlock().getLocation());
    }

    /**
     * Records a block location as modified when a block spreads into fire inside an active duel arena.
     * <p></p>
     * This ensures fire spread changes are tracked for the first matching running DuelArena so they can
     * be restored or handled by arena cleanup logic.
     *
     * @param event the BlockSpreadEvent to inspect for fire formation
     */
    @EventHandler
    public void onBlockSpread(final BlockSpreadEvent event) {
        if (event.getNewState().getType() == Material.FIRE) {
            trackModifiedBlockInArena(event.getBlock().getLocation());
        }
    }

    /**
     * Marks the location of a newly formed block as modified within the containing active duel arena.
     * <p></p>
     * This ensures the block change is tracked so arena state can be restored or otherwise managed after the duel.
     *
     * @param event the block formation event whose block location should be recorded as a modification
     */
    @EventHandler
    public void onBlockForm(final BlockFormEvent event) {
        trackModifiedBlockInArena(event.getBlock().getLocation());
    }

    /**
     * Records the location of a block that has faded so it is tracked as a modification inside an active duel arena.
     *
     * @param event the BlockFadeEvent representing a block changing state (for example ice or snow melting)
     */
    @EventHandler
    public void onBlockFade(final BlockFadeEvent event) {
        trackModifiedBlockInArena(event.getBlock().getLocation());
    }

    // TODO: Implement & fix the below methods (event handlers)

    /**
     * Cancel block ignition that occurs inside the region of any active duel arena.
     * <p></p>
     * Ignores duels in the STARTING or ENDING state; if the ignition location is contained
     * within an active duel's arena region, the event is cancelled and no further arenas
     * are checked.
     *
     * @param event the BlockIgniteEvent to evaluate and potentially cancel
     */
    @EventHandler
    public void onFireSpawn(final BlockIgniteEvent event) {
        for (final Game game : duelManager.getRunningGames().values()) {
            if (game.getGameState() == GameState.ENDING ||
                    game.getGameState() == GameState.STARTING) continue;

            final IArena arena = game.getArena();

            final BukkitCuboidRegion region = new BukkitCuboidRegion(
                    arena.getCorner1(),
                    arena.getCorner2()
            );

            if (region.contains(event.getBlock().getLocation())) {
                event.setCancelled(true);
                break;
            }
        }
    }

    /**
     * Records a world location as a modified block in the first active duel arena whose region contains it.
     *
     * <p>Skips duels that are in STARTING or ENDING states. Only arenas of type DuelArena are considered; when a DuelArena
     * contains the provided location (at block precision), that location is added to the arena's modified-locations and no
     * further duels are checked.</p>
     *
     * @param location the world location to record (evaluated at block precision)
     */
    private void trackModifiedBlockInArena(final Location location) {
        for (final Game game : duelManager.getRunningGames().values()) {
            if (game.getGameState() == GameState.STARTING ||
                    game.getGameState() == GameState.ENDING
            ) continue;

            final IArena arena = game.getArena();

            if (arena instanceof final DuelArena duelArena) {
                final BukkitCuboidRegion region = new BukkitCuboidRegion(
                        arena.getCorner1(),
                        arena.getCorner2()
                );

                if (region.contains(location)) {
                    duelArena.getModifiedLocations().add(location);
                    break;
                }
            }
        }
    }

    /**
     * Displays end-of-match titles to participants: a victory title to all players on the winning team
     * and a defeat title to all players on the defeated team, each including the final team scores.
     *
     * @param victimTeam the team that lost the match (shown the defeat title)
     * @param winnerTeam the team that won the match (shown the victory title)
     */
    private void showResults(
            final DuelTeam victimTeam,
            final DuelTeam winnerTeam
    ) {
        for (final Player winner : winnerTeam.getPlayers()) {
            winner.showTitle(Title.title(ColorUtils.parse("<#3B82F6><bold>VICTORY!"), ColorUtils.parse(String.format("<blue>%d <white>- <red>%d", winnerTeam.getScore(), victimTeam.getScore()))));
        }

        for (final Player loser : victimTeam.getPlayers()) {
            loser.showTitle(Title.title(ColorUtils.parse("<#fa1140><bold>DEFEAT!!"), ColorUtils.parse(String.format("<blue>%d <white>- <red>%d", victimTeam.getScore(), winnerTeam.getScore()))));
        }
    }


    /**
     * Check whether a duel team has no active participants remaining.
     *
     * A team is treated as having no active participants if it contains exactly one player
     * or every member is `null`, dead, offline, or in `SPECTATOR` game mode.
     *
     * @param team the team to evaluate
     * @return `true` if the team has no active participants (team size is one or every member is
     *         `null`, dead, offline, or in `SPECTATOR` mode), `false` otherwise
     */
    private boolean isWholeTeamDead(final DuelTeam team) {
        if (team.getUuids().size() == 1) {
            return true;
        }

        int deadPlayers = 0;
        for (final Player player : team.getPlayers()) {
            if (player == null || player.isDead() || !player.isOnline() || player.getGameMode() == GameMode.SPECTATOR) {
                deadPlayers += 1;
            }
        }

        return deadPlayers == team.getPlayers().size();
    }
}