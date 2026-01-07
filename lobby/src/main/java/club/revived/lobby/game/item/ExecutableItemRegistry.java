package club.revived.lobby.game.item;

import club.revived.lobby.Lobby;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * ExecutableItemRegistry
 *
 * @author yyuh - DL
 * @since 07.01.26
 */
public final class ExecutableItemRegistry {

    private static final Map<String, ExecutableItem> EXECUTABLE_ITEMS = new HashMap<>();

    /**
     * Register one or more executable items in the central registry.
     *
     * @param items executable items to register; each is stored under its `id()` and replaces any existing entry with the same id
     */
    public static void register(final ExecutableItem... items) {
        for (final ExecutableItem item : items) {
            EXECUTABLE_ITEMS.put(item.id(), item);
        }
    }

    /**
     * Retrieve the first registered executable item whose type equals the given type.
     *
     * @param type the executable item type to match
     * @return the first matching {@code ExecutableItem}, or {@code null} if none is registered
     */
    public static ExecutableItem byType(final ExecutableItemType type) {
        return EXECUTABLE_ITEMS.values()
                .stream()
                .filter(item -> item.type() == type)
                .toList()
                .getFirst();
    }

    /**
     * Registers an executable item in the central registry.
     *
     * If an item with the same id is already registered, it will be replaced by the provided item.
     *
     * @param item the executable item to register (its id() is used as the registry key)
     */
    public static void register(final ExecutableItem item) {
        EXECUTABLE_ITEMS.put(item.id(), item);
    }

    /**
     * Determines whether the given ItemStack is marked as an executable item.
     *
     * @param stack the item stack to check
     * @return `true` if the stack's persistent data container contains a string value for the `executable_item` key, `false` otherwise
     */
    public static boolean isExecutable(final ItemStack stack) {
        final var meta = stack.getItemMeta();

        if (meta == null) {
            return false;
        }

        final NamespacedKey key = new NamespacedKey(Lobby.getInstance(), "executable_item");

        final var data = stack.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        return data != null;
    }

    /**
     * Executes the registered executable item identified by the given ItemStack for the specified player.
     *
     * If the stack contains a persistent data value under the key "executable_item" that matches a registered
     * item id (case-insensitive), invokes that item's execute method with the provided player. If the stack
     * has no metadata, no such data value, or no matching registered item, this method does nothing.
     *
     * @param player the player for whom the executable item should be executed
     * @param stack  the ItemStack that may contain the executable item id in its persistent data container
     */
    public static void execute(final Player player, final ItemStack stack) {
        final var meta = stack.getItemMeta();

        if (meta == null) {
            return;
        }

        final NamespacedKey key = new NamespacedKey(Lobby.getInstance(), "executable_item");
        final var data = stack.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (data == null) {
            return;
        }

        for (final String id : EXECUTABLE_ITEMS.keySet()) {

            if (data.equalsIgnoreCase(id)) {
                final ExecutableItem item = EXECUTABLE_ITEMS.get(id);
                if (item != null) {
                    item.execute(player);
                }
            }
        }
    }
}