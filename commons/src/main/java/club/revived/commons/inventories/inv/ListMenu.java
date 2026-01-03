package club.revived.commons.inventories.inv;

import club.revived.commons.inventories.impl.PagedInventoryBuilder;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
@SuppressWarnings("unused")
public final class ListMenu extends PagedInventoryBuilder {

    public static ListMenu of(final String title) {
        return new ListMenu(title);
    }

    public ListMenu(final String title) {
        super(6, title);

        setPlaceholders(46, 47, 48, 49, 50, 51, 52);
    }

    @NotNull
    public ListMenu slot(int slot, final ItemBuilder item, final Consumer<InventoryClickEvent> event) {
        super.setPersistentItem(slot, item.build(), event);

        return this;
    }

    @NotNull
    public ListMenu addButtons(final Collection<? extends AbstractButton> items) {
        for (final AbstractButton button : items) {
            addItem(
                    button.getItemBuilder().build(),
                    button.getEventConsumer()
            );
        }

        return this;
    }

    @NotNull
    public ListMenu addItems(final Collection<? extends ItemBuilder> items) {
        for (final ItemBuilder item : items) {
            addItem(item.build());
        }

        return this;
    }

    @NotNull
    public ListMenu addItemStacks(final Collection<? extends ItemStack> items) {
        for (final ItemStack item : items) {
            addItem(item);
        }

        return this;
    }

    @Override
    public void open(final Player player) {
        this.update();
        super.open(player);
    }
}
