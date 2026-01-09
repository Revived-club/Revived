package club.revived.lobby.game;

import club.revived.commons.location.SerializedLocation;
import club.revived.lobby.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a fixed, named warp point within the lobby.
 *
 * <p>Each enum constant corresponds to a logical warp identifier that is
 * persisted in the database and resolved to a {@link Location} at runtime.
 * Warp locations are loaded lazily and may be updated dynamically during
 * server operation.</p>
 *
 * <p>Persistence is handled via {@link SerializedLocation} entries stored
 * using the {@link DatabaseManager}. Each warp is uniquely identified by
 * its {@code id} string.</p>
 *
 * <p><strong>Lifecycle notes:</strong></p>
 * <ul>
 *   <li>On enum initialization, {@link #update()} is invoked to resolve the
 *       stored location asynchronously.</li>
 *   <li>If no persisted value exists, a default fallback location is assigned.</li>
 *   <li>Calling {@link #set(Location)} immediately persists the new location.</li>
 * </ul>
 *
 * <p><strong>Threading considerations:</strong><br>
 * {@link #update()} performs an asynchronous database lookup. The internal
 * {@code location} field is therefore updated asynchronously and may be
 * temporarily {@code null} or outdated during early startup.</p>
 *
 * @author yyuh
 * @since 09.01.26
 */
public enum WarpLocation {

    BOX("box"),
    SPAWN("spawn"),
    KITROOM("kitroom"),
    SECRET("secret");

    private final String id;
    private Location location;

    /**
     * Creates a new warp entry and initiates loading of its persisted location.
     *
     * @param id the unique identifier used for database storage
     */
    WarpLocation(final String id) {
        this.id = id;
        update();
    }

    /**
     * Sets this warp's location and immediately persists it to storage.
     *
     * <p>The provided {@link Location} is stored under this warp's identifier,
     * overwriting any previously saved value.</p>
     *
     * @param location the new in-game location for this warp
     */
    public void set(final Location location) {
        this.location = location;

        final SerializedLocation serializedLocation = new SerializedLocation(
                this.id,
                location
        );

        DatabaseManager.getInstance().save(
                SerializedLocation.class,
                serializedLocation
        );
    }

    /**
     * Teleports the specified player to this warp's location.
     *
     * <p>This method assumes that the warp has already been resolved via
     * {@link #update()} and that {@link #getLocation()} is non-null.</p>
     *
     * @param player the player to teleport
     */
    public void teleport(final Player player) {
        player.teleport(this.location);
    }

    /**
     * Loads and applies this warp's location from persistent storage.
     *
     * <p>The lookup is performed asynchronously. If a stored
     * {@link SerializedLocation} exists, it is deserialized and applied.
     * Otherwise, a fallback location is assigned.</p>
     *
     * <p>This method mutates the internal state of the enum instance.</p>
     */
    public void update() {
        DatabaseManager.getInstance().get(SerializedLocation.class, this.id)
                .thenAccept(serializedLocation -> serializedLocation.ifPresentOrElse(
                        loc -> this.location = loc.location(),
                        () -> this.location = new Location(
                                Bukkit.getWorld("world"),
                                0,
                                90,
                                9
                        )));
    }

    /**
     * Returns the persistent identifier associated with this warp.
     *
     * @return the warp identifier
     */
    @NotNull
    public String getId() {
        return id;
    }

    /**
     * Returns the resolved in-game location for this warp.
     *
     * @return the current warp location
     */
    @NotNull
    public Location getLocation() {
        return location;
    }
}
