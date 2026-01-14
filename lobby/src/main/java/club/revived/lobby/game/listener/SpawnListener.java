package club.revived.lobby.game.listener;

import club.revived.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;

import java.util.List;

public final class SpawnListener implements Listener {

    private static final List<String> PROTECTED_WORLDS = List.of("world");

    /**
     * Registers this listener with the server plugin manager so its event handlers will receive events.
     *
     * <p>Registration uses the Sandbox plugin instance as the plugin registration owner.
     */
    public SpawnListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    /**
     * Prevents players from breaking blocks when the block is located in a protected world.
     *
     * @param event the block break event that will be cancelled if it occurs in a protected world
     */
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final var world = event.getBlock().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels block placement that occurs in a protected world.
     *
     * @param event the block placement event to check and potentially cancel
     */
    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final var world = event.getBlock().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents players from dropping items in protected worlds.
     *
     * @param event the player item drop event; will be cancelled if it occurs in a protected world
     */
    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        final var world = event.getPlayer().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents players from emptying buckets in protected worlds.
     *
     * @param event the bucket-empty event; cancelled if it occurs in a protected world
     */
    @EventHandler
    public void onBucketUse(final PlayerBucketEmptyEvent event) {
        final var world = event.getBlock().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels player bucket-fill actions when they occur in configured protected worlds.
     *
     * @param event the bucket-fill event; cancelled if the event's world is protected
     */
    @EventHandler
    public void onBucketFill(final PlayerBucketFillEvent event) {
        final var world = event.getBlock().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels any damage caused by an entity when the damage occurs in a protected world.
     *
     * @param event the entity damage event; will be cancelled if the damager's world is protected
     */
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        final var world = event.getEntity().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels any damage caused by an entity when the damage occurs in a protected world.
     *
     * @param event the entity damage event; will be cancelled if the damager's world is protected
     */
    @EventHandler
    public void onEntityDamage(final EntityDamageByEntityEvent event) {
        final var world = event.getDamager().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels an entity-caused explosion if it occurs in a protected world.
     *
     * @param event the explosion event to evaluate and possibly cancel
     */
    @EventHandler
    public void onExplosion(final EntityExplodeEvent event) {
        final var world = event.getLocation().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels explosions caused by blocks when they occur in a protected world.
     *
     * @param event the block explosion event to evaluate and cancel if its world is protected
     */
    @EventHandler
    public void onBlockExplode(final BlockExplodeEvent event) {
        final var world = event.getBlock().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents hanging entities (frames, paintings, etc.) from being broken by entities in protected worlds.
     *
     * @param event the event representing a hanging entity being broken by another entity
     */
    @EventHandler
    public void onHangingBreak(final HangingBreakByEntityEvent event) {
        final var world = event.getRemover().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents players from manipulating armor stands in configured protected worlds.
     *
     * @param event the player-armor-stand interaction event to check and possibly cancel
     */
    @EventHandler
    public void onArmorStandManipulate(final PlayerArmorStandManipulateEvent event) {
        final var world = event.getRightClicked().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels block changes caused by entities (trampling) when they occur in a protected world.
     *
     * @param event the EntityChangeBlockEvent representing the entity-caused block change
     */
    @EventHandler
    public void onTrample(final EntityChangeBlockEvent event) {
        final var world = event.getBlock().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents creatures from spawning in worlds listed in PROTECTED_WORLDS.
     *
     * @param event the CreatureSpawnEvent to inspect and cancel when its world is protected
     */
    @EventHandler
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        final var world = event.getEntity().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents creates from hungering in worlds listed in PROTECTED_WORLDS.
     *
     * @param event the FoodLevelChangeEvent to inspect and cancel when its world is protected
     */
    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        final var world = event.getEntity().getWorld();

        if (PROTECTED_WORLDS.contains(world.getName())) {
            event.setCancelled(true);
        }
    }
}