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

    private CyclingButton(int slot, List<ItemStack> items, Consumer<ItemStack> onCycle) {
        this.slot = slot;
        this.items = items;
        this.onCycle = onCycle;
    }

    private CyclingButton() {
    }

    @NotNull
    public static CyclingButton of(
            final int slot,
            final @NotNull List<ItemStack> items, final Consumer<ItemStack> onCycle
    ) {
        return new CyclingButton(slot, items, onCycle);
    }

    @NotNull
    public static CyclingButton of(
            final int slot,
            final @NotNull List<ItemStack> items
    ) {
        return new CyclingButton(slot, items, null);
    }

    @NotNull
    public static CyclingButton of(
            final int slot,
            final @NotNull ItemStack... items
    ) {
        return new CyclingButton(slot, Arrays.asList(items), null);
    }

    @NotNull
    public static CyclingButton of() {
        return new CyclingButton();
    }

    @NotNull
    public CyclingButton setSlot(final int slot) {
        this.slot = slot;
        return this;
    }

    @NotNull
    public CyclingButton setItems(final List<ItemStack> items) {
        this.items = items;
        return this;
    }

    @NotNull
    public CyclingButton setItems(final ItemStack... items) {
        this.items = Arrays.asList(items);
        return this;
    }

    @NotNull
    public CyclingButton onCycle(final Consumer<ItemStack> onCycle) {
        this.onCycle = onCycle;
        return this;
    }

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
