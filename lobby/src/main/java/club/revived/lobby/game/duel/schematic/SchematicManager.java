package club.revived.lobby.game.duel.schematic;

import club.revived.lobby.Lobby;
import club.revived.lobby.database.DatabaseManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;

import java.io.File;
import java.nio.file.Files;

/**
 * SchematicProvider
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class SchematicManager {

    private static SchematicManager instance;

    public SchematicManager() {
        instance = this;
    }

    public void saveArena(
            final String id,
            final Location corner1,
            final Location corner2,
            final Location spawn1,
            final Location spawn2,
            final ArenaType arenaType
    ) {
        final var arenaSchem = new DuelArenaSchematic(
                id,
                corner1,
                corner2,
                spawn1,
                spawn2,
                arenaType
        );

        DatabaseManager.getInstance().save(DuelArenaSchematic.class, arenaSchem);
        this.saveSchematic(corner1, corner2, id);
    }

    public void saveSchematic(
            final Location bukkitCorner1,
            final Location bukkitCorner2,
            final String id
            ) {
        final var world = BukkitAdapter.adapt(bukkitCorner1.getWorld());
        final var corner1 = BukkitAdapter.adapt(bukkitCorner1);
        final var corner2 = BukkitAdapter.adapt(bukkitCorner2);

        final var region = new CuboidRegion(
                corner1.toVector().toBlockPoint(),
                corner2.toVector().toBlockPoint()
        );

        final BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(corner1.toVector().toBlockPoint());

        final ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(world, region, clipboard, region.getMinimumPoint());
        forwardExtentCopy.setCopyingBiomes(true);
        forwardExtentCopy.setCopyingEntities(false);

        try {
            Operations.complete(forwardExtentCopy);

            final var schemFile = new File(
                    Lobby.getInstance().getDataFolder(),
                    "schem/" + id + ".schem"
            );

            if (!schemFile.exists()) {
                schemFile.createNewFile();
            }

            final ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(Files.newOutputStream(schemFile.toPath()));
            writer.write(clipboard);
            writer.close();

            final WorldeditSchematic schematic = new WorldeditSchematic(id, schemFile);
            DatabaseManager.getInstance().save(WorldeditSchematic.class, schematic);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SchematicManager getInstance() {
        if (instance == null) {
            return new SchematicManager();
        }

        return instance;
    }
}