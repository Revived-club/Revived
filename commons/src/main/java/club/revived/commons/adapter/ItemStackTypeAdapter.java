package club.revived.commons.adapter;

import club.revived.commons.serialization.ItemSerializer;
import com.google.gson.*;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class ItemStackTypeAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    /**
     * Converts a JSON primitive string (as produced by ItemSerializer) into an ItemStack.
     *
     * @param json the JSON element containing the serialized ItemStack as a primitive string
     * @param typeOfT ignored (Gson serialization type)
     * @param context ignored (Gson deserialization context)
     * @return the ItemStack represented by the JSON string
     * @throws JsonParseException if the JSON is not a primitive string or deserialization fails
     */
    @Override
    public ItemStack deserialize(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        return ItemSerializer.deserialize(json.getAsJsonPrimitive().getAsString());
    }

    @Override
    public JsonElement serialize(
            final ItemStack src,
            final Type typeOfSrc,
            final JsonSerializationContext context
    ) {
        return new JsonPrimitive(ItemSerializer.serializeItemStack(src));
    }
}