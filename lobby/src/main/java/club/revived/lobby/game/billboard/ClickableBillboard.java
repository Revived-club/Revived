package club.revived.lobby.game.billboard;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.lobby.Lobby;
import club.revived.lobby.game.duel.DuelManager;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.duel.QueueType;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.messaging.impl.IsQueuedRequest;
import club.revived.lobby.service.messaging.impl.IsQueuedResponse;
import club.revived.lobby.service.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class ClickableBillboard {

    private final WrapperEntity entity;
    private final List<WrapperEntity> associated;
    private final QueueBillboardBuilder billboardBuilder;
    private final BiConsumer<WrapperEntity, Player> action;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private Location location;

    /**
     * Creates a ClickableBillboard that manages a main entity, its associated entities, visual builder, location, and interaction behavior.
     *
     * @param entity           the main billboard entity wrapper
     * @param associated       entities visually or functionally associated with the billboard (may be empty)
     * @param billboardBuilder builder responsible for producing the billboard's displayed content
     * @param location         the world location at which the billboard is placed
     * @param action           the action to execute when a player interacts with the billboard; receives the main entity and the interacting player
     */
    public ClickableBillboard(
            WrapperEntity entity,
            List<WrapperEntity> associated,
            QueueBillboardBuilder billboardBuilder,
            Location location,
            BiConsumer<WrapperEntity, Player> action
    ) {
        this.entity = entity;
        this.associated = associated;
        this.billboardBuilder = billboardBuilder;
        this.action = action;
        this.location = location;
    }

    /**
     * Handle a player's interaction with the billboard: enforce per-player cooldown, play a click
     * sound, execute the configured interaction action, and schedule a metadata update.
     *
     * @param player the player who interacted with the billboard
     */
    public void interact(Player player) {
        if (System.currentTimeMillis() < cooldowns.getOrDefault(player.getUniqueId(), 0L)) {
            error(player, "<red>Cooldown!");
            return;
        }

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 1000);

        player.playSound(player, Sound.BLOCK_LEVER_CLICK, 1, 1);

        action.accept(entity, player);

        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            update(player);
        }, 20L);
    }

    /**
     * Shows an error message on the billboard, applies a 1-second player cooldown, and plays a short shake animation.
     * <p></p>
     * Updates the billboard's visible text to the provided error string with a red background, plays the villager "no" sound to the player, and schedules three client-side teleports to produce a brief left/right shake (executed after 2, 4 and 6 ticks). Sets the player's cooldown to one second and, after the animation (and when the cooldown expires), triggers an update of the billboard display for that player.
     *
     * @param player the player to receive the error feedback and animation
     * @param text   the error text to display on the billboard
     */
    private void error(final Player player, final String text) {
        final var entityMeta = EntityMeta.createMeta(entity.getEntityId(), EntityTypes.TEXT_DISPLAY);
        final var meta = (TextDisplayMeta) entityMeta;

        final long newCooldown = System.currentTimeMillis() + 1000;
        cooldowns.put(player.getUniqueId(), newCooldown);

        meta.setNotifyAboutChanges(false);

        meta.setText(ColorUtils.parse(text));

        final int hex = Integer.parseInt("fc4141", 16);
        final int a = 0x40;
        final var color = Color.fromARGB(hex).setAlpha(a).asARGB();

        meta.setBackgroundColor(color);

        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);

        final WrapperPlayServerEntityMetadata packet = meta.createPacket();
        EntityLib.getApi().getPacketEvents().getPlayerManager().sendPacket(player, packet);

        final Location original = SpigotConversionUtil.toBukkitLocation(player.getWorld(), entity.getLocation());

        final float yaw = original.getYaw();
        final Vector3d right = shakeVector(yaw, 0.01);
        final Vector3d left = shakeVector(yaw, -0.01);

        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            sendTeleport(player, entity.getEntityId(),
                    original.clone().add(right.getX(), 0, right.getZ()));
        }, 2L);

        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            sendTeleport(player, entity.getEntityId(),
                    original.clone().add(left.getX(), 0, left.getZ()));
        }, 4L);

        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            sendTeleport(player, entity.getEntityId(), original);

            final long currentCooldown = cooldowns.getOrDefault(player.getUniqueId(), 0L);
            if (System.currentTimeMillis() >= currentCooldown - 100) {
                update(player);
            } else {
                final long delay = (currentCooldown - System.currentTimeMillis()) / 50;
                if (delay > 0) {
                    Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
                        if (System.currentTimeMillis() >= cooldowns.getOrDefault(player.getUniqueId(), 0L) - 100) {
                            update(player);
                        }
                    }, delay);
                }
            }
        }, 6L);
    }

    /**
     * Plays a short client-side click animation for this billboard for the given player.
     * <p>
     * Sends a metadata update and schedules two small, timed client-only teleports that
     * momentarily offset the billboard and then restore it to simulate a click.
     */
    private void animateClick(final Player player) {
        final var entityMeta = EntityMeta.createMeta(entity.getEntityId(), EntityTypes.TEXT_DISPLAY);
        final var meta = (TextDisplayMeta) entityMeta;

        meta.setNotifyAboutChanges(false);

        final WrapperPlayServerEntityMetadata packet = meta.createPacket();
        EntityLib.getApi().getPacketEvents().getPlayerManager().sendPacket(player, packet);

        final Location original = SpigotConversionUtil.toBukkitLocation(player.getWorld(), entity.getLocation());

        final float yaw = original.getYaw();
        final Vector3d clickVec = clickVector(yaw, -0.01);

        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            sendTeleport(player, entity.getEntityId(),
                    original.clone().add(clickVec.getX(), 0, clickVec.getZ()));
        }, 4L);

        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            sendTeleport(player, entity.getEntityId(), original);
        }, 6L);
    }

    /**
     * Updates the billboard's text and background color for a specific player to reflect current queue status.
     * <p>
     * Sets the TEXT_DISPLAY metadata (text: "Queueing..." or "Click Me") and a semi-transparent green background,
     * then sends the metadata packet only to the provided player.
     *
     * @param player the recipient player whose client will receive the metadata update
     */
    public void update(final Player player) {
        final var entityMeta = EntityMeta.createMeta(entity.getEntityId(), EntityTypes.TEXT_DISPLAY);
        final var meta = (TextDisplayMeta) entityMeta;

        meta.setNotifyAboutChanges(false);

        Cluster.getInstance().getMessagingService().sendRequest(
                "queue-service",
                new IsQueuedRequest(player.getUniqueId()),
                IsQueuedResponse.class
        ).thenAccept(isQueuedResponse -> {
            Bukkit.getScheduler().runTask(Lobby.getInstance(), () -> {
                if (isQueuedResponse.queued()) {
                    meta.setText(ColorUtils.parse("<green>Queueing...</green>"));
                } else {
                    meta.setText(ColorUtils.parse("<green>Click Me</green>"));
                }

                final int hex = Integer.parseInt("8dfc98", 16);
                final int a = 0x40;
                final var color = Color.fromARGB(hex).setAlpha(a).asARGB();

                meta.setBackgroundColor(color);

                final WrapperPlayServerEntityMetadata packet = meta.createPacket();

                EntityLib.getApi().getPacketEvents().getPlayerManager().sendPacket(
                        player,
                        packet
                );
            });
        });

    }

    /**
     * Send an entity-teleport packet to a specific player updating the entity's position and rotation.
     *
     * @param player   the player who will receive the packet
     * @param entityId the ID of the entity to teleport
     * @param loc      the target location (includes x, y, z, yaw, and pitch)
     */
    private void sendTeleport(
            final Player player,
            final int entityId,
            final Location loc
    ) {
        final Vector3d pos = new Vector3d(loc.getX(), loc.getY(), loc.getZ());
        final WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(
                entityId, pos, loc.getYaw(), loc.getPitch(), false
        );
        EntityLib.getApi().getPacketEvents().getPlayerManager().sendPacket(player, packet);
    }

    /**
     * Moves the billboard and its associated entities to the specified location, placing the main billboard entity slightly forward and above that position.
     *
     * @param location the target world location to move the billboard to
     */
    public void move(final Location location) {
        for (final var associated : this.associated) {
            associated.teleport(SpigotConversionUtil.fromBukkitLocation(location));
        }

        final Location buttonLocation = location.clone();
        final double forwardOffset = 0.01;
        final double verticalOffset = 0.1;

        buttonLocation.add(
                location.getDirection().multiply(forwardOffset)
        );
        buttonLocation.add(0, verticalOffset, 0);
        this.entity.teleport(SpigotConversionUtil.fromBukkitLocation(buttonLocation));


        this.location = location;
    }

    /**
     * Updates the billboard's yaw and applies that rotation to the main entity and all associated entities.
     * <p>
     * Sets the provided yaw on the main entity, repositions the billboard to reflect the new orientation,
     * updates each associated entity's yaw and teleports them to their rotated locations, and stores the new location.
     *
     * @param yaw   new yaw angle in degrees
     * @param world world used to convert and store the updated location
     */
    public void setYaw(
            final float yaw,

            final World world
    ) {
        final var loc = entity.getLocation();
        loc.setYaw(yaw);

        move(SpigotConversionUtil.toBukkitLocation(world, loc));

        for (final var associated : this.associated) {
            final var mainLoc = associated.getLocation();

            mainLoc.setYaw(yaw);
            associated.teleport(mainLoc);
        }

        this.location = SpigotConversionUtil.toBukkitLocation(world, loc);
    }

    /**
     * Update this billboard's pitch and apply the change to the main and associated entities.
     *
     * @param pitch the new pitch angle in degrees to apply
     * @param world the world used to convert and apply the updated location
     */
    public void setPitch(
            final float pitch,
            final World world

    ) {
        final var loc = entity.getLocation();
        loc.setPitch(pitch);

        move(SpigotConversionUtil.toBukkitLocation(world, loc));

        for (final var associated : this.associated) {
            final var mainLoc = associated.getLocation();

            mainLoc.setPitch(pitch);
            associated.teleport(mainLoc);
        }

        this.location = SpigotConversionUtil.toBukkitLocation(world, loc);
    }

    /**
     * Compute a horizontal offset vector from a yaw angle and magnitude.
     *
     * @param yaw       the yaw angle in degrees (rotation around the vertical axis)
     * @param magnitude the distance/magnitude of the offset
     * @return a Vector3d representing the horizontal offset for the given yaw and magnitude; the y component is zero
     */
    @NotNull
    private Vector3d clickVector(
            final float yaw,
            final double magnitude
    ) {
        final double radians = Math.toRadians(yaw);
        return new Vector3d(
                -Math.sin(radians) * magnitude,
                0,
                Math.cos(radians) * magnitude
        );
    }

    /**
     * Computes a horizontal shake offset vector based on the given yaw and magnitude.
     *
     * @param yaw       the yaw in degrees used to derive orientation
     * @param magnitude the desired length of the offset
     * @return a Vector3d with Y = 0 whose X/Z components form a vector of length `magnitude`
     * oriented opposite the forward direction defined by `yaw`
     */
    @NotNull
    private Vector3d shakeVector(
            final float yaw,
            final double magnitude
    ) {
        final double radians = Math.toRadians(yaw);
        return new Vector3d(
                -Math.cos(radians) * magnitude,
                0,
                -Math.sin(radians) * magnitude
        );
    }

    /**
     * Gets the main billboard entity.
     *
     * @return the main WrapperEntity representing this billboard
     */
    public WrapperEntity entity() {
        return entity;
    }

    /**
     * Get the entities associated with this billboard.
     *
     * @return the internal list of associated {@code WrapperEntity} instances
     */
    public List<WrapperEntity> associated() {
        return associated;
    }

    /**
     * Gets the billboard builder responsible for constructing and updating this billboard's visuals.
     *
     * @return the {@link QueueBillboardBuilder} used by this billboard
     */
    public QueueBillboardBuilder billboardBuilder() {
        return billboardBuilder;
    }

    /**
     * Create a BiConsumer that invokes this billboard's interaction for a given entity and player.
     *
     * @return a BiConsumer which, when called with a `WrapperEntity` and `Player`, executes the billboard interaction for those arguments
     */
    public BiConsumer<WrapperEntity, Player> wrapperEntityConsumer() {
        return this::interact;
    }

    /**
     * Get the current location of the billboard.
     *
     * @return the billboard's current {@link Location}
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Invokes the billboard's interaction flow for the given player; the provided entity parameter is not used.
     *
     * @param entity the clicked billboard entity (ignored by this method)
     * @param player the player who initiated the interaction
     */
    private void interact(WrapperEntity entity, Player player) {
        interact(player);
    }
}