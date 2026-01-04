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

    @Override
    public Location deserialize(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        return LocationSerializer.deserialize(json.getAsJsonPrimitive().getAsString());
    }

    @Override
    public JsonElement serialize(
            final Location src,
            final Type typeOfSrc,
            final JsonSerializationContext context
    ) {
        return new JsonPrimitive(LocationSerializer.serialize(src));
    }
}