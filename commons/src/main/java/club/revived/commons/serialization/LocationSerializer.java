package club.revived.commons.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.MalformedParametersException;

/**
 * LocationSerializer
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class LocationSerializer {

    /**
     * Encodes a Location into a colon-separated string in the form "worldName:x:y:z:yaw:pitch".
     *
     * @param location the Location to encode; must not be null and must have a non-null world
     * @return the encoded location string in the format "worldName:x:y:z:yaw:pitch"
     * @throws MalformedParametersException if {@code location} is null or its world is null
     */
    @NotNull
    public static String serialize(final Location location) {
        if (location == null || location.getWorld() == null) {
            throw new MalformedParametersException();
        }

        final String worldName = location.getWorld().getName();
        final double x = location.getX();
        final double y = location.getY();
        final double z = location.getZ();
        final float yaw = location.getYaw();
        final float pitch = location.getPitch();

        return worldName + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
    }

    /**
     * Parses a colon-separated location string and returns the corresponding Bukkit Location.
     *
     * The expected format is "worldName:x:y:z:yaw:pitch".
     *
     * @param encodedLocation the encoded location string in the format "worldName:x:y:z:yaw:pitch"
     * @return the Location represented by the encoded string
     * @throws MalformedParametersException if {@code encodedLocation} is null or empty
     * @throws IllegalArgumentException if the string does not contain exactly six parts, if numeric parsing fails, or if the referenced world cannot be found
     */
    @NotNull
    public static Location deserialize(final String encodedLocation) {
        if (encodedLocation == null || encodedLocation.isEmpty()) {
            throw new MalformedParametersException();
        }

        final String[] parts = encodedLocation.split(":");

        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid location format.");
        }

        final String worldName = parts[0];
        final double x = Double.parseDouble(parts[1]);
        final double y = Double.parseDouble(parts[2]);
        final double z = Double.parseDouble(parts[3]);
        final float yaw = Float.parseFloat(parts[4]);
        final float pitch = Float.parseFloat(parts[5]);

        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + worldName);
        }

        return new Location(world, x, y, z, yaw, pitch);
    }
}