package club.revived.commons.serialization;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class ItemSerializer {

    @NotNull
    public static String serializeItemStacks(final @NotNull Map<Integer, ItemStack> itemStacks) {
        final ItemStack[] items = itemStacks.values().toArray(new ItemStack[0]);

        final List<String> serializedItems = new ArrayList<>();
        for (final ItemStack item : items) {
            final String serialized = serializeItemStack(item);
            serializedItems.add(serialized);
        }

        return String.join(";", serializedItems);
    }



    @NotNull
    @SuppressWarnings("CallToPrintStackTrace")
    public static String serializeItemStack(final @NotNull ItemStack itemStack) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream bukkitObjectOutputStream;

        try {
            bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
            bukkitObjectOutputStream.writeObject(itemStack);
            bukkitObjectOutputStream.close();

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @NotNull
    public static Map<Integer, ItemStack> deserializeItemStackMap(final @NotNull String data) {
        final Map<Integer, ItemStack> kitContents = new HashMap<>();
        final String[] serializedItems = data.split(";");

        for (int i = 0; i < serializedItems.length; i++) {
            final ItemStack item = deserialize(serializedItems[i]);
            kitContents.put(i, item);
        }

        return kitContents;
    }

    @NotNull
    @SuppressWarnings("CallToPrintStackTrace")
    public static ItemStack deserialize(final @NotNull String data) {
        final byte[] bytes = Base64.getDecoder().decode(data);
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            final BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
            final ItemStack itemStack = (ItemStack) bukkitObjectInputStream.readObject();
            bukkitObjectInputStream.close();

            return itemStack;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
