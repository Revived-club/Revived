package club.revived.commons.worldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * SchematicPaster
 *
 * @author yyuh
 * @since 09.01.26
 */
public final class WorldEditUtils {

    /**
     * Retrieve the two corner blocks that define the given player's current WorldEdit region selection.
     *
     * @param player the Bukkit player whose WorldEdit selection will be used
     * @return an array with two Blocks: the maximum corner at index 0 and the minimum corner at index 1, or `null` if the player has no complete selection
     */
    @NotNull
    public static Block[] getRegionCorners(final Player player) {
        final com.sk89q.worldedit.entity.Player pl = BukkitAdapter.adapt(player);
        final com.sk89q.worldedit.world.World wld = BukkitAdapter.adapt(player.getWorld());

        try {
            if (WorldEdit.getInstance().getSessionManager().get(pl).getSelection(wld) != null) {
                final Region region = WorldEdit.getInstance().getSessionManager().get(pl).getSelection(wld);
                final Location corner1 = BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint());
                final Location corner2 = BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint());

                return new Block[] {corner1.getBlock(), corner2.getBlock()};
            }
        } catch (final IncompleteRegionException e) {
            return null;
        }

        return null;
    }
}