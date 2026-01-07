package club.revived.commons.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * BukkitCuboidRegion
 *
 * @author yyuh
 * @since 07.01.26
 */
public final class BukkitCuboidRegion {

    private final World world;

    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    /**
     * Creates a cuboid region defined by two corner locations.
     *
     * <p>The region is defined by the block coordinates of the two provided locations and includes
     * all blocks between them (inclusive).</p>
     *
     * @param a one corner of the cuboid
     * @param b the opposite corner of the cuboid
     * @throws IllegalArgumentException if either location is null, either location has a null world,
     *                                  or the two locations are not in the same world
     */
    public BukkitCuboidRegion(Location a, Location b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Locations cannot be null");
        }
        if (a.getWorld() == null || b.getWorld() == null) {
            throw new IllegalArgumentException("Locations must have a world");
        }
        if (!a.getWorld().equals(b.getWorld())) {
            throw new IllegalArgumentException("Locations must be in the same world");
        }

        this.world = a.getWorld();

        this.minX = Math.min(a.getBlockX(), b.getBlockX());
        this.minY = Math.min(a.getBlockY(), b.getBlockY());
        this.minZ = Math.min(a.getBlockZ(), b.getBlockZ());

        this.maxX = Math.max(a.getBlockX(), b.getBlockX());
        this.maxY = Math.max(a.getBlockY(), b.getBlockY());
        this.maxZ = Math.max(a.getBlockZ(), b.getBlockZ());
    }

    /**
     * Gets the world that contains this cuboid region.
     *
     * @return the {@link World} in which this region resides
     */
    public World getWorld() {
        return world;
    }

    /**
 * Gets the region's minimum X coordinate.
 *
 * @return the inclusive minimum X block coordinate of the region
 */
public int getMinX() { return minX; }
    /**
 * Gets the region's inclusive minimum Y coordinate.
 *
 * @return the inclusive minimum Y coordinate of the region
 */
public int getMinY() { return minY; }
    /**
 * Gets the inclusive minimum Z block coordinate of the region.
 *
 * @return the region's minimum Z coordinate (inclusive)
 */
public int getMinZ() { return minZ; }

    /**
 * Gets the inclusive maximum X block-coordinate of this cuboid region.
 *
 * @return the maximum X coordinate (inclusive)
 */
public int getMaxX() { return maxX; }
    /**
 * Gets the region's maximum Y coordinate (inclusive).
 *
 * @return the maximum Y block coordinate within the region
 */
public int getMaxY() { return maxY; }
    /**
 * Gets the region's maximum Z coordinate.
 *
 * @return the inclusive maximum Z coordinate of the region
 */
public int getMaxZ() { return maxZ; }

    /**
     * Creates a Location at the region's minimum (inclusive) block coordinates in the region's world.
     *
     * @return a new Location representing (minX, minY, minZ) in this region's World
     */
    public Location getMinimumPoint() {
        return new Location(world, minX, minY, minZ);
    }

    /**
     * Get the region's maximum corner as a Location.
     *
     * The returned Location is in the region's world and uses the region's inclusive maximum block
     * coordinates (maxX, maxY, maxZ).
     *
     * @return a Location at (maxX, maxY, maxZ) in the region's world
     */
    public Location getMaximumPoint() {
        return new Location(world, maxX, maxY, maxZ);
    }

    /**
     * Determines whether the given location lies within this cuboid region.
     *
     * @param loc the location to test; may be null
     * @return {@code true} if {@code loc} is non-null, its world matches this region's world,
     *         and its block coordinates are within the region's inclusive bounds; {@code false} otherwise
     */
    public boolean contains(final Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(world)) return false;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    /**
     * Checks whether the given block lies within this cuboid region.
     *
     * @param block the block to test for containment; may be {@code null}
     * @return {@code true} if the block is non-null and its location is inside the region, {@code false} otherwise
     */
    public boolean contains(Block block) {
        return block != null && contains(block.getLocation());
    }

    /**
     * Computes the total number of block positions contained by this cuboid (inclusive bounds).
     *
     * @return the total count of blocks in the region
     */
    public long getVolume() {
        return (long) (maxX - minX + 1)
                * (long) (maxY - minY + 1)
                * (long) (maxZ - minZ + 1);
    }

    /**
     * Iterates all blocks contained in this cuboid region.
     *
     * The returned Iterable produces an Iterator that visits every Block in the region's world
     * whose block coordinates are within the region's inclusive bounds. Iteration advances in
     * row-major order: x increments fastest, then z, then y.
     *
     * @return an Iterable of all Blocks within this region's inclusive bounds
     * @throws NoSuchElementException if the iterator's {@code next()} is called when no blocks remain
     */
    @NotNull
    public Iterable<Block> getBlocks() {
        return () -> new Iterator<>() {

            private int x = minX;
            private int y = minY;
            private int z = minZ;

            @Override
            public boolean hasNext() {
                return y <= maxY;
            }

            @NotNull
            @Override
            public Block next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                final Block block = world.getBlockAt(x, y, z);

                x++;
                if (x > maxX) {
                    x = minX;
                    z++;
                    if (z > maxZ) {
                        z = minZ;
                        y++;
                    }
                }

                return block;
            }
        };
    }
}