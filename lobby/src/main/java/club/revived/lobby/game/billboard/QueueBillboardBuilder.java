package club.revived.lobby.game.billboard;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.lobby.Lobby;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.duel.QueueType;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.messaging.impl.QueuedAmountRequest;
import club.revived.lobby.service.messaging.impl.QueuedAmountResponse;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.meta.other.InteractionMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;

public final class QueueBillboardBuilder {

    private KitType kitType;
    private Location location;
    private BiConsumer<WrapperEntity, Player> wrapperEntityConsumer;

    /**
     * Create a new QueueBillboardBuilder instance.
     *
     * @return a fresh QueueBillboardBuilder for constructing queue billboards
     */
    public static QueueBillboardBuilder billboard() {
        return new QueueBillboardBuilder();
    }

    /**
     * Set the target spawn location for the billboard.
     *
     * @param location the Bukkit location where the billboard will be placed
     * @return this builder instance
     */
    public QueueBillboardBuilder location(final Location location) {
        this.location = location;
        return this;
    }

    /**
     * Set the kit type used by the billboard being built.
     *
     * @param kitType the KitType whose queue information will be displayed on the billboard
     * @return the builder instance for chaining
     */
    public QueueBillboardBuilder kiType(final KitType kitType) {
        this.kitType = kitType;
        return this;
    }

    /**
     * Builds and displays a queue billboard for the configured kit type at the configured location.
     * <p>
     * Constructs and spawns the main text display that shows the current solo queue count, creates and spawns
     * an interactive button associated with that display, stores the provided callback for interaction events,
     * and schedules a repeating task to refresh the displayed queue count.
     *
     * @param wrapperEntityConsumer callback invoked with the wrapper entity and the player when an interaction occurs
     */
    public void build(final BiConsumer<WrapperEntity, Player> wrapperEntityConsumer) {
        final var spawnLocation = SpigotConversionUtil.fromBukkitLocation(location);

        final WrapperEntity textDisplay = new WrapperEntity(EntityTypes.TEXT_DISPLAY);
        final TextDisplayMeta textMeta = (TextDisplayMeta) textDisplay.getEntityMeta();

//        textMeta.setText(ColorUtils.parse("""
//                <#3B82F6><bold><type> Queue<reset>
//                <gray><queue>/2
//
//                """
//                .replace("<type>", this.kitType.getBeautifiedName())
//                .replace("<queue>", String.valueOf(QueueManager.getInstance().getQueued(
//                        this.kitType,
//                        QueueManager.QueueType.SOLO
//                )))
//        ));

        textDisplay.spawn(spawnLocation);
        textMeta.setShadow(true);

        final Location buttonLocation = location.clone();

        final double forwardOffset = 0.01;
        final double verticalOffset = 0.1;

        this.wrapperEntityConsumer = wrapperEntityConsumer;

        buttonLocation.add(
                location.getDirection().multiply(forwardOffset)
        );
        buttonLocation.add(0, verticalOffset, 0);

        createButton(buttonLocation, textDisplay);
        queueUpdateTask(textDisplay);
    }

    /**
     * Periodically updates the given billboard's displayed text to show the current solo queue count for this builder's KitType.
     * <p>
     * The task runs every 20 ticks and replaces the billboard's text with a formatted string containing the kit name and current queued players.
     *
     * @param billboard the wrapper entity whose TextDisplayMeta will be updated on each interval
     */
    private void queueUpdateTask(final WrapperEntity billboard) {
        Bukkit.getScheduler().runTaskTimer(Lobby.getInstance(), () -> {
            Cluster.getInstance().getMessagingService()
                    .sendRequest(
                            "queue-service",
                            new QueuedAmountRequest(kitType, QueueType.SOLO),
                            QueuedAmountResponse.class
                    )
                    .thenAccept(queuedAmountResponse -> {
                        final TextDisplayMeta textMeta = (TextDisplayMeta) billboard.getEntityMeta();

                        textMeta.setText(ColorUtils.parse("""
                                <#3B82F6><bold><type> Queue<reset>
                                <gray><queue>/2
                                
                                """
                                .replace("<type>", this.kitType.getBeautifiedName())
                                .replace("<queue>", String.valueOf(queuedAmountResponse.amount()))
                        ));
                    });

        }, 0, 20L);
    }

    /**
     * Creates and registers an interactive "Click Me" button at the given location and associates it with the provided billboard.
     * <p>
     * The method spawns a styled text display and an interaction hitbox, registers a ClickableBillboard with the BillboardManager
     * (both by kit type and by interaction entity id), and makes the entities visible to all currently online players.
     *
     * @param location  the Bukkit location where the button and its interaction area should be spawned
     * @param billboard the main billboard entity to associate with the created interactive button
     */
    private void createButton(final Location location, final WrapperEntity billboard) {
        final var spawnLocation = SpigotConversionUtil.fromBukkitLocation(location);

        final WrapperEntity textDisplay = new WrapperEntity(EntityTypes.TEXT_DISPLAY);
        final TextDisplayMeta textMeta = (TextDisplayMeta) textDisplay.getEntityMeta();

        final Component text = ColorUtils.parse("<green>Click Me</green>");
        textMeta.setText(text);
        textMeta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.FIXED);

        final int hex = Integer.parseInt("8dfc98", 16);
        final int a = 0x40;
        final var color = Color.fromARGB(hex).setAlpha(a).asARGB();

        textMeta.setBackgroundColor(color);
        textMeta.setGlowColorOverride(color);
        textMeta.setGlowing(true);
        textMeta.setShadow(true);
        textDisplay.spawn(spawnLocation);

        final WrapperEntity interaction = new WrapperEntity(EntityTypes.INTERACTION);
        final InteractionMeta interactionMeta = (InteractionMeta) interaction.getEntityMeta();

        final String plainText = PlainTextComponentSerializer.plainText().serialize(text);
        final float width = plainText.length() * 0.15f;
        final float height = 0.25f;

        interactionMeta.setHeight(height + 0.1f);
        interactionMeta.setWidth(width + 0.1f);
        interaction.spawn(spawnLocation);

        BillboardManager.getInstance().getQueueBillboards().put(
                this.kitType,
                new ClickableBillboard(textDisplay, List.of(interaction, textDisplay, billboard), this, location, this.wrapperEntityConsumer)
        );

        BillboardManager.getInstance().getBillboards().put(
                interaction.getEntityId(),
                new ClickableBillboard(textDisplay, List.of(interaction, textDisplay, billboard), this, location, this.wrapperEntityConsumer)
        );

        for (final Player player : Bukkit.getOnlinePlayers()) {
            interaction.addViewer(player.getUniqueId());
            textDisplay.addViewer(player.getUniqueId());
            billboard.addViewer(player.getUniqueId());
        }
    }
}