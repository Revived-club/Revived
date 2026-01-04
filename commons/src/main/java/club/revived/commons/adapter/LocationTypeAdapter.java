package club.revived.commons.adapter;

import club.revived.commons.serialization.LocationSerializer;
import com.google.gson.*;
import org.bukkit.Location;

import java.lang.reflect.Type;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class LocationTypeAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    /**
     * Reconstructs a Bukkit Location from a JSON primitive string produced by LocationSerializer.
     *
     * @param json JSON element expected to be a primitive string produced by LocationSerializer
     * @return the deserialized Location
     * @throws JsonParseException if the JSON is not a valid serialized Location
     */
    @Override
    public Location deserialize(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        return LocationSerializer.deserialize(json.getAsJsonPrimitive().getAsString());
    }

    /**
     * Converts the given Location to a JSON primitive containing its serialized string form.
     *
     * @param src the Location to serialize
     * @return a JsonPrimitive holding the Location's serialized string
     */
    @Override
    public JsonElement serialize(
            final Location src,
            final Type typeOfSrc,
            final JsonSerializationContext context
    ) {
        return new JsonPrimitive(LocationSerializer.serialize(src));
    }
}