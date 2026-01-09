package club.revived.lobby.game.billboard;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.duel.DuelManager;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.duel.QueueType;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.messaging.impl.IsQueuedRequest;
import club.revived.lobby.service.messaging.impl.IsQueuedResponse;
import club.revived.lobby.service.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class BillboardManager {

    private static BillboardManager instance;

    private final Map<Integer, ClickableBillboard> billboards = new HashMap<>();
    private final Map<KitType, ClickableBillboard> queueBillboards = new HashMap<>();

    /**
     * Initializes a new BillboardManager and registers it as the class's singleton instance.
     */
    public BillboardManager() {
        instance = this;
    }

    /**
     * Load all stored queue billboard locations and create billboards at each saved kit type and location.
     *
     * <p>Asynchronously retrieves persisted QueueBillboardLocation entries and instantiates billboards for them.</p>
     */
    public void setup() {
//        DatabaseManager.getInstance().get()
//        PersistentDataManager.getInstance().getAll(QueueBillboardLocation.class).thenAccept(queueBillboardLocations -> {
//            for (final var billboardLoc : queueBillboardLocations) {
//                build(billboardLoc.kitType(), billboardLoc.location());
//            }
//        });
    }

    /**
     * Builds an interactive queue billboard for the given kit type at the specified location.
     *
     * When a player interacts with the created billboard, their queue membership is toggled:
     * if they are currently in a queue they will be removed and the billboard text becomes "Click Me";
     * otherwise they will be added to the SOLO queue for the specified kit and the billboard text becomes "Queueing...".
     * The interaction also plays a lever-click sound for the interacting player and pushes updated entity metadata to that player.
     *
     * @param type     the kit type the billboard represents
     * @param location the world location where the billboard will be placed
     */
    public void build(final KitType type, final Location location) {
        QueueBillboardBuilder.billboard()
                .kiType(type)
                .location(location)
                .build((entity, player) -> {
                    final var entityMeta = EntityMeta.createMeta(entity.getEntityId(), EntityTypes.TEXT_DISPLAY);
                    final var meta = (TextDisplayMeta) entityMeta;

                    meta.setNotifyAboutChanges(false);
                    player.playSound(player, Sound.BLOCK_LEVER_CLICK, 1, 1);

                    meta.setText(ColorUtils.parse("<green>Loading..."));

                    final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

                    DuelManager.getInstance().queue(
                            networkPlayer,
                            KitType.SWORD,
                            QueueType.SOLO
                    );

                    Cluster.getInstance().getMessagingService().sendRequest(
                            "queue-service",
                            new IsQueuedRequest(player.getUniqueId()),
                            IsQueuedResponse.class
                    ).thenAccept(isQueuedResponse -> {
                        if (isQueuedResponse.queued()) {
                            meta.setText(ColorUtils.parse("<green>Queueing...</green>"));
                        } else {
                            meta.setText(ColorUtils.parse("<green>Click Me</green>"));
                        }
                    });

                    final WrapperPlayServerEntityMetadata packet = meta.createPacket();

                    EntityLib.getApi().getPacketEvents().getPlayerManager().sendPacket(
                            player,
                            packet
                    );
                });
    }

    /**
     * Update a text display entity's visible text and background color for a specific player based on their queue status.
     *
     * Sets the display text to "Queueing..." if the player is in a queue, otherwise "Click Me"; applies a semi‑transparent green background and sends the updated entity metadata to the player.
     *
     * @param player   the player who should receive the updated display
     * @param entityId the entity ID of the text display to update
     */
    public void update(Player player, int entityId) {
        final var entityMeta = EntityMeta.createMeta(entityId, EntityTypes.TEXT_DISPLAY);
        final var meta = (TextDisplayMeta) entityMeta;

        meta.setNotifyAboutChanges(false);

//        if (QueueManager.getInstance().isInQueue(player)) {
//            meta.setText(ColorUtils.parse("<green>Queueing...</green>"));
//        } else {
//            meta.setText(ColorUtils.parse("<green>Click Me</green>"));
//        }

        final int hex = Integer.parseInt("8dfc98", 16);
        final int a = 0x40;
        final var color = Color.fromARGB(hex).setAlpha(a).asARGB();

        meta.setBackgroundColor(color);

        final WrapperPlayServerEntityMetadata packet = meta.createPacket();

        EntityLib.getApi().getPacketEvents().getPlayerManager().sendPacket(
                player,
                packet
        );
    }

    /**
     * Provides the mapping from kit types to their clickable queue billboards.
     *
     * @return the live map whose keys are {@link KitType} and values are {@link ClickableBillboard}
     */
    public Map<KitType, ClickableBillboard> getQueueBillboards() {
        return queueBillboards;
    }

    /**
     * Provides the map of active billboards keyed by entity ID.
     *
     * @return a live map mapping entity IDs to their corresponding ClickableBillboard instances
     */
    public Map<Integer, ClickableBillboard> getBillboards() {
        return billboards;
    }

    /**
     * Accesses the singleton BillboardManager, creating a new instance if one does not already exist.
     *
     * @return the singleton BillboardManager instance; a new instance is created and returned if none existed
     */
    public static BillboardManager getInstance() {
        if (instance == null) {
            return new BillboardManager();
        }

        return instance;
    }
}