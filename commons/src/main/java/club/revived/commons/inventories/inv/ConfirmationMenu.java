package club.revived.commons.inventories.inv;

import club.revived.commons.inventories.impl.InventoryBuilder;
import club.revived.commons.inventories.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
@SuppressWarnings("unused")
public final class ConfirmationMenu extends InventoryBuilder {

    private static final ItemStack DEFAULT_CONFIRM_BUTTON = ItemBuilder.item(Material.ARROW)
            .name("<green>Confirm")
            .build();

    private static final ItemStack DEFAULT_CANCEL_BUTTON = ItemBuilder.item(Material.ARROW)
            .name("<red>CANCEL")
            .build();

    private static final ItemStack DEFAULT_CENTER_ITEM = ItemBuilder.item(Material.ORANGE_CANDLE)
            .name("Accept this")
            .build();

    @NotNull
    private ItemStack confirmItem = DEFAULT_CONFIRM_BUTTON;

    @NotNull
    private ItemStack cancelItem = DEFAULT_CANCEL_BUTTON;

    @NotNull
    private ItemStack centerItem = DEFAULT_CENTER_ITEM;

    private boolean placeholders = false;

    @Nullable
    private Consumer<Boolean> onComplete;

    @NotNull
    public static ConfirmationMenu of(final @NotNull String title) {
        return new ConfirmationMenu(title);
    }

    public ConfirmationMenu(final @NotNull String title) {
        super(27, title);
    }

    @NotNull
    public ConfirmationMenu setConfirmItem(final @NotNull ItemBuilder item) {
        this.confirmItem = item.build();
        return this;
    }

    @NotNull
    public ConfirmationMenu setCancelItem(final @NotNull ItemBuilder item) {
        this.cancelItem = item.build();
        return this;
    }

    @NotNull
    public ConfirmationMenu setCenterItem(final @NotNull ItemBuilder item) {
        this.centerItem = item.build();
        return this;
    }

    @NotNull
    public ConfirmationMenu withPlaceholders(final boolean placeholders) {
        this.placeholders = placeholders;
        return this;
    }

    @NotNull
    public ConfirmationMenu initialize() {
        if(placeholders) {
            for (int i = 0; i < this.getInventory().getSize(); i++) {
                setPlaceholder(i);
            }
        }

        setItem(11, confirmItem, event -> {
            onComplete.accept(true);
            event.setCancelled(true);
        });

        setItem(13, centerItem, event -> event.setCancelled(true));

        setItem(15, cancelItem, event -> {
            onComplete.accept(false);
            event.setCancelled(true);
        });
        return this;
    }

    @NotNull
    public ConfirmationMenu onComplete(final @NotNull Consumer<Boolean> onComplete) {
        this.onComplete = onComplete;
        return this;
    }
}
