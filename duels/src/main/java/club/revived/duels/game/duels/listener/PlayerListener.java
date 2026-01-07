package club.revived.duels.game.duels.listener;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.duels.Duels;
import club.revived.duels.game.arena.ArenaType;
import club.revived.duels.game.arena.IArena;
import club.revived.duels.game.arena.impl.DuelArena;
import club.revived.duels.game.duels.*;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
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

    public PlayerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Duels.getInstance());
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof final Player player) {
            if (!duelManager.isDueling(player)) return;

            final Duel duel = duelManager.getDuel(player);

            if (duel == null) {
                return;
            }

            if (saturatedKits.contains(duel.getKitType())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void handle(EntityExplodeEvent event) {
        event.setYield(0);
    }

    @EventHandler
    public void handle(BlockExplodeEvent event) {
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
    public void handle(EntityChangeBlockEvent event) {
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

        final Duel duel = duelManager.getDuel(victim);

        if (duel == null) {
            return;
        }

        final var victimTeam = duel.getTeam(victim);

        if (victimTeam.hasPlayer(attacker)) {
            attacker.sendRichMessage("<red>You can't attack your Team Mates!");
            event.setCancelled(true);
        }
    }

    /**
     * Handle a player quitting: clear per-player cached data, suppress the quit message, and resolve any active duel state.
     *
     * <p>If the player is part of an ongoing duel, this will determine whether their team is fully eliminated; if so,
     * it increments the winning team's score, marks the duel as ending, displays match results, and ends the duel.
     * If the team is not fully eliminated, it notifies remaining duel participants that the player quit and was removed
     * from the duel.</p>
     *
     * @param event the PlayerQuitEvent triggered when a player disconnects
     */
    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        event.quitMessage(null);

        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);

        if (duel == null || duel.getGameState() == GameState.ENDING) return;

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
    }

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

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {
        final Player player = event.getPlayer();

        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);
        if (duel == null || duel.getGameState() != GameState.RUNNING) return;

        event.deathMessage(null);
        event.setCancelled(true);

        player.setGameMode(GameMode.SPECTATOR);

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
    }

    @EventHandler
    public void onAchievement(final PlayerAdvancementDoneEvent event) {
        event.message(null);
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        event.joinMessage(null);

        final Player player = event.getPlayer();

        if (player.isDead()) {
            player.spigot().respawn();
        }
    }

    @EventHandler
    public void onProjectileLand(final ProjectileHitEvent event) {
        if (event.getEntity().getType() != EntityType.SNOWBALL) return;
        if (event.getHitBlock() == null) return;

        final Block block = event.getHitBlock();
        if (block.getType() == Material.SNOW_BLOCK) {
            block.setType(Material.AIR);
        }
    }

    @EventHandler
    public void onEntityHit(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;
        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);
        if (duel == null || duel.getGameState() != GameState.RUNNING) return;

        if (duel.getKitType() != KitType.SPLEEF) return;

        if (event.getDamager() instanceof org.bukkit.entity.Snowball) {
            event.setDamage(0.1);
            player.setHealth(20.0);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFallDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;
        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);
        if (duel == null) return;

        if (duel.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && duel.getKitType() == KitType.SPLEEF) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);
        if (duel == null) return;

        if (duel.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGenericDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);
        if (duel == null) return;

        if (duel.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);
        if (duel == null) return;

        if (duel.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickUp(final PlayerAttemptPickupItemEvent event) {
        final Player player = event.getPlayer();
        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);
        if (duel == null) return;

        if (duel.getGameState() == GameState.ENDING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final var player = event.getPlayer();

        if (!duelManager.isDueling(player)) return;

        final var duel = duelManager.getDuel(player);

        if (duel == null) {
            return;
        }

        if (duel.getGameState() == GameState.ENDING ||
                duel.getGameState() == GameState.STARTING) {
            event.setCancelled(true);
            return;
        }

        final IArena arena = duel.getArena();

        if (arena.getArenaType() == ArenaType.INTERACTIVE) {
            return;
        }


        if (arena instanceof final DuelArena duelArena) {
            final Location location = event.getBlock().getLocation();
            duelArena.getModifiedLocations().add(location);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();

        if (!duelManager.isDueling(player)) return;

        final Duel duel = duelManager.getDuel(player);

        if (duel == null) {
            return;
        }

        if (duel.getGameState() == GameState.ENDING ||
                duel.getGameState() == GameState.STARTING) {
            event.setCancelled(true);
            return;
        }

        final IArena arena = duel.getArena();

        if (arena instanceof final DuelArena duelArena) {
            final Location location = event.getBlock().getLocation();

            if (!duelArena.getModifiedLocations().contains(location)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(final BlockFromToEvent event) {
        final Block block = event.getToBlock();
        final Material newType = block.getType();

        if (newType == Material.COBBLESTONE || newType == Material.STONE || newType == Material.OBSIDIAN || newType == Material.BASALT) {
            trackModifiedBlockInArena(block.getLocation());
        }
    }

    @EventHandler
    public void onBlockBurn(final BlockBurnEvent event) {
        trackModifiedBlockInArena(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockSpread(final BlockSpreadEvent event) {
        if (event.getNewState().getType() == Material.FIRE) {
            trackModifiedBlockInArena(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockForm(final BlockFormEvent event) {
        trackModifiedBlockInArena(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockFade(final BlockFadeEvent event) {
        trackModifiedBlockInArena(event.getBlock().getLocation());
    }

    // TODO: Implement & fix the below methods (event handlers)

    /**
     * Cancels a block ignite event when the ignition occurs inside an active duel arena
     * unless the arena's type is EXPLOSIVE or MINI_GAME.
     * <p></p>
     * Iterates ongoing duels and ignores duels that are starting or have ended. If the
     * ignition location is contained within any such duel's arena region, the event is
     * cancelled.
     *
     * @param event the block ignite event to evaluate and potentially cancel
     */
    @EventHandler
    public void onFireSpawn(final BlockIgniteEvent event) {
        for (final Duel duel : duelManager.getRunningDuels().values()) {
            if (duel.getGameState() == GameState.ENDING ||
                    duel.getGameState() == GameState.STARTING) continue;

            final IArena arena = duel.getArena();

            final BlockVector3 position = BukkitAdapter.adapt(event.getBlock().getLocation()).toVector().toBlockPoint();

            final CuboidRegion region = new CuboidRegion(
                    BukkitAdapter.adapt(arena.getCorner1()).toVector().toBlockPoint(),
                    BukkitAdapter.adapt(arena.getCorner2()).toVector().toBlockPoint()
            );

            if (region.contains(position)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    /**
     * Registers the given world location as a modified block in the first matching ongoing duel arena that contains it.
     * <p></p>
     * Iterates ongoing duels, skipping those that are starting or already ended. Only arenas of type Arena are considered;
     * arenas with ArenaType.EXPLOSIVE or ArenaType.MINI_GAME are ignored. If the location falls inside an arena's cuboid
     * region, the corresponding block position is added to that arena's modified-block tracking and iteration stops.
     *
     * @param location the world location to record (evaluated at block precision)
     */
    private void trackModifiedBlockInArena(final Location location) {
        for (final Duel duel : duelManager.getOngoingDuels()) {
            if (duel.getGameState() == GameState.STARTING ||
                    duel.getGameState() == GameState.ENDING
            ) continue;

            final IArena arena = duel.getArena();

            if (arena instanceof final DuelArena duelArena) {
                BlockVector3 position = BukkitAdapter.adapt(location).toVector().toBlockPoint();

                CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(arena.getCorner1()).toVector().toBlockPoint(), BukkitAdapter.adapt(arena.getCorner2()).toVector().toBlockPoint());

                if (region.contains(position)) {
                    duelArena.getModifiedLocations().add(location);
                    break;
                }
            }
        }
    }

    /**
     * Display end-of-match titles and play corresponding sounds for winners, losers, and spectators.
     *
     * <p>Shows a victory title and sound to all players on the winning team, a defeat title and sound
     * to all players on the defeated team, and an end-match summary title to spectators observing the duel.</p>
     *
     * @param victimTeam the team that lost the match
     * @param winnerTeam the team that won the match
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
     * Determine whether a team has no active participants remaining.
     *
     * <p>Teams are considered inactive if they contain exactly one player or if every player
     * is null, dead, offline, or in spectator mode.</p>
     *
     * @param team the team to evaluate
     * @return `true` if the team has exactly one player or all players are null, dead, offline,
     *         or in spectator mode; `false` otherwise
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