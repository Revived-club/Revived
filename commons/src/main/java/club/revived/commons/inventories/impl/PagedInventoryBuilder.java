package club.revived.commons.inventories.impl;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
@SuppressWarnings("unused")
public abstract class PagedInventoryBuilder extends InventoryBuilder {

    private static final ItemStack DEFAULT_PREVIOUS_PAGE_ITEM = ItemBuilder.item(Material.ARROW)
            .name("<#3B82F6>Previous Page")
            .lore(ColorUtils.parse("<white>Click to go to the next page"))
            .build();

    private static final ItemStack DEFAULT_NEXT_PAGE_ITEM = ItemBuilder.item(Material.ARROW)
            .name("<#3B82F6>Next Page")
            .lore(ColorUtils.parse("<white>Click to go to the next page")).build();

    private final Map<Integer, PageItem> items = new HashMap<>();
    private final List<Consumer<PagedInventoryBuilder>> updateHandlers = new ArrayList<>();
    private final Map<Integer, PageItem> persistentItems = new HashMap<>();

    private int currentPage = 0;
    private final int itemsPerPage;
    private ItemStack previousPageItem = DEFAULT_PREVIOUS_PAGE_ITEM;
    private ItemStack nextPageItem = DEFAULT_NEXT_PAGE_ITEM;
    private int previousPageSlot = -1;
    private int nextPageSlot = -1;

    public PagedInventoryBuilder(int rows, String title) {
        super(rows * 9, title);
        this.itemsPerPage = (rows * 9) - 9;
        initializeNavigation();
    }

    private void initializeNavigation() {
        if (previousPageSlot == -1) {
            previousPageSlot = getInventory().getSize() - 9;
        }
        if (nextPageSlot == -1) {
            nextPageSlot = getInventory().getSize() - 1;
        }
    }

    public void addItem(ItemStack item) {
        addItem(item, e -> e.setCancelled(true));
    }

    public void addItem(ItemStack item, Consumer<InventoryClickEvent> handler) {
        int page = items.size() / itemsPerPage;
        int slot = items.size() % itemsPerPage;
        setItem(page, slot, item, handler);
    }

    public void setItem(int page, int slot, ItemStack item) {
        setItem(page, slot, item, e -> {});
    }

    public void setItem(int page, int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        if (slot >= itemsPerPage) {
            throw new IllegalArgumentException("Slot " + slot + " is out of bounds for items per page (" + itemsPerPage + ")");
        }

        int index = page * itemsPerPage + slot;
        items.put(index, new PageItem(item, handler));

        if (page == currentPage) {
            update();
        }
    }

    public void nextPage(Player player) {
        if (hasNextPage()) {
            currentPage++;
            update();
        }
    }

    public void previousPage(Player player) {
        if (hasPreviousPage()) {
            currentPage--;
            update();
        }
    }

    public boolean hasNextPage() {
        return items.keySet().stream().anyMatch(i -> i >= (currentPage + 1) * itemsPerPage);
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    public void update() {
        getInventory().clear();

        for (Map.Entry<Integer, PageItem> entry : persistentItems.entrySet()) {
            int slot = entry.getKey();
            PageItem item = entry.getValue();
            getInventory().setItem(slot, item.item());
            itemHandlers.put(slot, item.handler());
        }

        for (int i = 0; i < itemsPerPage; i++) {
            itemHandlers.remove(i);
        }

        for (int i = 0; i < itemsPerPage; i++) {
            int index = currentPage * itemsPerPage + i;
            PageItem pageItem = items.get(index);

            if (pageItem != null) {
                getInventory().setItem(i, pageItem.item());
                itemHandlers.put(i, pageItem.handler());
            }
        }

        setItem(previousPageSlot, previousPageItem, e -> {
            e.setCancelled(true);
            previousPage((Player) e.getWhoClicked());
        });

        setItem(nextPageSlot, nextPageItem, e -> {
            e.setCancelled(true);
            nextPage((Player) e.getWhoClicked());
        });

        updateHandlers.forEach(handler -> handler.accept(this));
    }

    public void setPlaceholders(int... slots) {
        for (int slot : slots) {
            setPersistentItem(slot, ItemBuilder.item(Material.GRAY_STAINED_GLASS_PANE)
                    .tooltip(false).build(), event -> event.setCancelled(true)
            );
        }
    }

    public void setPersistentItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        if (slot < 0 || slot >= getInventory().getSize()) {
            throw new IllegalArgumentException("Slot " + slot + " is out of bounds for inventory size " + getInventory().getSize());
        }

        persistentItems.put(slot, new PageItem(item, handler));
        update();
    }

    public void setPlaceholder(int slot){
        setPersistentItem(slot, ItemBuilder.item(Material.GRAY_STAINED_GLASS_PANE).tooltip(false).build(),
                event -> event.setCancelled(true));
    }


    public void addUpdateHandler(Consumer<PagedInventoryBuilder> handler) {
        updateHandlers.add(handler);
    }

    public PagedInventoryBuilder setPreviousPageItem(ItemStack item) {
        this.previousPageItem = item;
        return this;
    }

    public PagedInventoryBuilder setNextPageItem(ItemStack item) {
        this.nextPageItem = item;
        return this;
    }

    public PagedInventoryBuilder setNavigationSlots(int previousSlot, int nextSlot) {
        this.previousPageSlot = previousSlot;
        this.nextPageSlot = nextSlot;
        return this;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        if (items.isEmpty()) return 0;
        int maxIndex = items.keySet().stream().max(Integer::compare).orElse(0);
        return (maxIndex / itemsPerPage) + 1;
    }

    private record PageItem(ItemStack item, Consumer<InventoryClickEvent> handler) {
    }
}
