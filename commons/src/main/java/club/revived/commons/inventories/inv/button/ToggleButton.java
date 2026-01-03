package club.revived.commons.inventories.inv.button;

import club.revived.commons.inventories.impl.InventoryBuilder;
import club.revived.commons.inventories.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public class ToggleButton implements Button {

    private static final ItemStack DEFAULT_ITEM = ItemBuilder.item(Material.STONE)
            .name("<dark_purple>Epic Setting")
            .build();

    private static final ItemStack ON_ITEM = ItemBuilder.item(Material.LIME_DYE)
            .name("<green>Enabled")
            .glow()
            .build();

    private static final ItemStack OFF_ITEM = ItemBuilder.item(Material.RED_DYE)
            .name("<red>Disabled")
            .glow()
            .build();

    private ItemStack stack = DEFAULT_ITEM;
    private ItemStack offItem = OFF_ITEM;
    private ItemStack onItem = ON_ITEM;
    private int displaySlot = 0;
    private int settingSlot = 1;

    private Consumer<Boolean> onComplete;

    private AtomicBoolean value = new AtomicBoolean(false);

    @NotNull
    public static ToggleButton of() {
        return new ToggleButton();
    }

    @NotNull
    public ToggleButton setDisplayItem(final ItemStack stack) {
        this.stack = stack;
        return this;
    }

    @NotNull
    public ToggleButton setOnItem(final ItemStack stack) {
        this.onItem = stack;
        return this;
    }

    @NotNull
    public ToggleButton setOffItem(final ItemStack stack) {
        this.offItem = stack;
        return this;
    }

    @NotNull
    public ToggleButton setDisplaySlot(final int slot) {
        this.displaySlot = slot;
        return this;
    }

    @NotNull
    public ToggleButton setButtonSlot(final int slot) {
        this.settingSlot = slot;
        return this;
    }

    @NotNull
    public ToggleButton setBoolean(final boolean value) {
        this.value = new AtomicBoolean(value);
        return this;
    }

    @NotNull
    public ToggleButton setBoolean(final AtomicBoolean value) {
        this.value = value;
        return this;
    }

    @NotNull
    public ToggleButton onToggle(final Consumer<Boolean> onComplete) {
        this.onComplete = onComplete;
        return this;
    }


    @Override
    public void build(final InventoryBuilder builder) {
        builder.setItem(this.displaySlot, stack, event -> {
            event.setCancelled(true);
        });

        builder.setSwapItem(this.settingSlot, this.onItem, this.offItem, value.get(),
                event -> {
                    event.setCancelled(true);
                    value.set(true);
                    if (this.onComplete != null) {
                        this.onComplete.accept(true);
                    }
                },
                event -> {
                    event.setCancelled(true);
                    value.set(false);
                    if (this.onComplete != null) {
                        this.onComplete.accept(true);
                    }
                });
    }
}
