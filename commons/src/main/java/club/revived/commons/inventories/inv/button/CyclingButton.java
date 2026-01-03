package club.revived.commons.inventories.inv.button;

import club.revived.commons.inventories.impl.InventoryBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A button that cycles through a list of items when clicked.
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class CyclingButton implements Button {

    private int slot;
    private List<ItemStack> items;
    private Consumer<ItemStack> onCycle;

    /**
     * Create a CyclingButton configured for the given inventory slot, item cycle, and optional callback.
     *
     * @param slot    the inventory slot index where the button will be placed
     * @param items   the list of ItemStack options to cycle through (may be null or empty)
     * @param onCycle optional callback invoked with the current ItemStack each time the button is cycled (may be null)
     */
    private CyclingButton(int slot, List<ItemStack> items, Consumer<ItemStack> onCycle) {
        this.slot = slot;
        this.items = items;
        this.onCycle = onCycle;
    }

    /**
     * Creates a CyclingButton with default (unset) fields.
     */
    private CyclingButton() {
    }

    /**
     * Create a CyclingButton configured with the given slot, items, and optional cycle callback.
     *
     * @param slot    the inventory slot where the button will be placed
     * @param items   the list of ItemStack entries to cycle through
     * @param onCycle an optional callback invoked with the current ItemStack when the button cycles; may be null
     * @return a CyclingButton configured to cycle through the supplied items at the specified slot
     */
    @NotNull
    public static CyclingButton of(
            final int slot,
            final @NotNull List<ItemStack> items, final Consumer<ItemStack> onCycle
    ) {
        return new CyclingButton(slot, items, onCycle);
    }

    /**
     * Create a CyclingButton configured with the given inventory slot and cycle items.
     *
     * @param slot  the inventory slot index where the button will be placed
     * @param items the list of ItemStack instances to cycle through
     * @return a CyclingButton configured with the specified slot and items and no on-cycle callback
     */
    @NotNull
    public static CyclingButton of(
            final int slot,
            final @NotNull List<ItemStack> items
    ) {
        return new CyclingButton(slot, items, null);
    }

    /**
     * Create a CyclingButton for the given inventory slot that cycles through the provided items.
     *
     * @param slot  the inventory slot index where the button will be placed
     * @param items the items to cycle through (varargs)
     * @return      a CyclingButton configured with the given slot and items and no onCycle callback
     */
    @NotNull
    public static CyclingButton of(
            final int slot,
            final @NotNull ItemStack... items
    ) {
        return new CyclingButton(slot, Arrays.asList(items), null);
    }

    /**
     * Create an empty CyclingButton instance.
     *
     * <p>The returned instance has no slot, no items, and no onCycle callback configured.</p>
     *
     * @return a new CyclingButton with default (unset) slot, items, and onCycle
     */
    @NotNull
    public static CyclingButton of() {
        return new CyclingButton();
    }

    /**
     * Set the inventory slot where the button will be placed.
     *
     * @param slot the target inventory slot index
     * @return this CyclingButton instance for method chaining
     */
    @NotNull
    public CyclingButton setSlot(final int slot) {
        this.slot = slot;
        return this;
    }

    /**
     * Sets the list of ItemStack instances that the button will cycle through when clicked.
     *
     * @param items the list of items to cycle through; replaces the current items
     * @return this CyclingButton for method chaining
     */
    @NotNull
    public CyclingButton setItems(final List<ItemStack> items) {
        this.items = items;
        return this;
    }

    /**
     * Set the sequence of ItemStack entries that the button will cycle through when clicked.
     *
     * @param items the ItemStack entries, in the order they should be cycled
     * @return this CyclingButton instance for fluent configuration
     */
    @NotNull
    public CyclingButton setItems(final ItemStack... items) {
        this.items = Arrays.asList(items);
        return this;
    }

    /**
     * Set the callback to invoke when the button cycles to an item.
     *
     * @param onCycle the consumer that receives the currently selected {@link ItemStack} when cycling occurs;
     *                may be {@code null} to clear the callback
     * @return this {@code CyclingButton} instance for method chaining
     */
    @NotNull
    public CyclingButton onCycle(final Consumer<ItemStack> onCycle) {
        this.onCycle = onCycle;
        return this;
    }

    /**
     * Registers this cycling button in the given InventoryBuilder so the configured slot cycles through the configured items when clicked.
     *
     * If no items are configured the method does nothing. When a configured slot is clicked the click event is cancelled and, if present, the {@code onCycle} callback is invoked with the current item at this slot.
     */
    @Override
    public void build(final InventoryBuilder builder) {
        if (items == null || items.isEmpty()) {
            return;
        }
        builder.setMultiItem(this.slot, this.items, event -> {
            event.setCancelled(true);
            if (onCycle != null) {
                onCycle.accept(builder.getItem(this.slot));
            }
        });
    }
}