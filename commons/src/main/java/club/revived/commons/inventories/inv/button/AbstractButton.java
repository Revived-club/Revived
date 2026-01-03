package club.revived.commons.inventories.inv.button;

import club.revived.commons.inventories.impl.InventoryBuilder;
import club.revived.commons.inventories.util.ItemBuilder;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class AbstractButton implements Button {

    private final ItemBuilder itemBuilder;
    private final Consumer<InventoryClickEvent> eventConsumer;
    private final int slot;

    /**
     * Creates a button configuration for an inventory at the given slot with the provided item builder and click handler.
     *
     * @param slot the inventory slot index where the button will be placed
     * @param itemBuilder builds the visual ItemStack to display for this button
     * @param eventConsumer handles click events for this button
     */
    public AbstractButton(
            final int slot,
            final ItemBuilder itemBuilder,
            final Consumer<InventoryClickEvent> eventConsumer
    ) {
        this.slot = slot;
        this.itemBuilder = itemBuilder;
        this.eventConsumer = eventConsumer;
    }

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    /**
     * Gets the click event handler for this button.
     *
     * @return the Consumer invoked when the button is clicked
     */
    public Consumer<InventoryClickEvent> getEventConsumer() {
        return eventConsumer;
    }

    /**
     * Applies this button's item and click handler to the given InventoryBuilder at the configured slot.
     *
     * @param builder the InventoryBuilder to receive the item and its click handler
     */
    @Override
    public void build(final InventoryBuilder builder) {
        builder.setItem(this.slot, this.itemBuilder.build(), this.eventConsumer);
    }
}