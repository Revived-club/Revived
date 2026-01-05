package club.revived.commons.inventories.inv;

import club.revived.commons.inventories.impl.InventoryBuilder;
import club.revived.commons.inventories.inv.button.Button;
import club.revived.commons.inventories.util.ItemBuilder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;


/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class AbstractMenu extends InventoryBuilder {

    private final int rows;

    public AbstractMenu(final int rows, final String title) {
        super(rows * 9, title);
        this.rows = rows;
    }

    /**
     * Create a new AbstractMenu with the specified number of rows and title.
     *
     * @param rows  the number of rows in the inventory grid
     * @param title the title displayed for the inventory
     * @return      a new AbstractMenu configured with the given rows and title
     */
    @NotNull
    public static AbstractMenu of(final int rows, final String title) {
        return new AbstractMenu(rows, title);
    }

    /**
     * Adds a Button to this menu.
     *
     * @param button the Button to add to the menu
     * @return this AbstractMenu instance for method chaining
     */
    @NotNull
    public AbstractMenu button(final Button button) {
        button.build(this);
        return this;
    }

    @NotNull
    public AbstractMenu slot(final int slot, final ItemBuilder item, final Consumer<InventoryClickEvent> event) {
        setItem(slot, item.build(), event);
        return this;
    }

    @NotNull
    public AbstractMenu fillEmpty(final ItemBuilder item) {
        for (int i = 0; i < (rows * 9); i++) {

            if (!getItem(i).isEmpty()) continue;

            setItem(i, item.build(), event -> event.setCancelled(true));
        }
        return this;
    }

    @NotNull
    public AbstractMenu onClose(final Consumer<InventoryCloseEvent> event) {
        addCloseHandler(event);
        return this;
    }

    @NotNull
    public AbstractMenu onClick(final Consumer<InventoryClickEvent> event) {
        addClickHandler(event);
        return this;
    }
}