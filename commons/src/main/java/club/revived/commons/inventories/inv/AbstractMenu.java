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
        super(rows, title);
        this.rows = rows;
    }

    @NotNull
    public static AbstractMenu of(final int rows, final String title) {
        return new AbstractMenu(rows, title);
    }

    @NotNull
    public AbstractMenu button(final int slot, final Button button) {
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
