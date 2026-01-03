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

    public Consumer<InventoryClickEvent> getEventConsumer() {
        return eventConsumer;
    }

    @Override
    public void build(final InventoryBuilder builder) {
        builder.setItem(this.slot, this.itemBuilder.build(), this.eventConsumer);
    }
}
