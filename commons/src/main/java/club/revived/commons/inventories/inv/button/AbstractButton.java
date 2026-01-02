package club.revived.commons.inventories.inv.button;

import club.revived.commons.inventories.util.ItemBuilder;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class AbstractButton {

    private final ItemBuilder itemBuilder;
    private final Consumer<InventoryClickEvent> eventConsumer;

    public AbstractButton(ItemBuilder itemBuilder, Consumer<InventoryClickEvent> eventConsumer) {
        this.itemBuilder = itemBuilder;
        this.eventConsumer = eventConsumer;
    }

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    public Consumer<InventoryClickEvent> getEventConsumer() {
        return eventConsumer;
    }
}
