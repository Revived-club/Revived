package club.revived.commons.location;

import org.bukkit.Location;

/**
 * SerializedLocation
 *
 * @author yyuh
 * @since 09.01.26
 */
public record SerializedLocation(
        String id,
        Location location
) {}
