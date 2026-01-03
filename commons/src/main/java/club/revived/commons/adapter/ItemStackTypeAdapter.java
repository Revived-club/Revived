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