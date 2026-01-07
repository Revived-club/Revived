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

    public World getWorld() {
        return world;
    }

    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }

    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    public Location getMinimumPoint() {
        return new Location(world, minX, minY, minZ);
    }

    public Location getMaximumPoint() {
        return new Location(world, maxX, maxY, maxZ);
    }

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

    public boolean contains(Block block) {
        return block != null && contains(block.getLocation());
    }

    public long getVolume() {
        return (long) (maxX - minX + 1)
                * (long) (maxY - minY + 1)
                * (long) (maxZ - minZ + 1);
    }

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
