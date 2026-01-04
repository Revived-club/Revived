package club.revived.commons.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;

import java.io.File;
import java.nio.file.Files;

/**
 * SchematicPaster
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class SchematicPaster {

    /**
     * Paste a WorldEdit schematic file into the Bukkit world at the given location.
     *
     * If the file's clipboard format is recognized, the schematic is read and pasted
     * so its origin aligns with the block coordinates of the provided location.
     * If the format cannot be determined, the method performs no action.
     *
     * @param file the schematic file to paste (e.g. .schem, .schematic)
     * @param location the target Bukkit location; the location's block coordinates are used as the paste origin
     * @throws RuntimeException if an error occurs while reading the file or executing the paste (the original exception is wrapped)
     */
    public static void paste(
            final File file,
            final Location location
    ) {
        final BlockVector3 to = BukkitAdapter.adapt(location).toVector().toBlockPoint();
        final ClipboardFormat format = ClipboardFormats.findByFile(file);

        if (format != null)
            try {
                final ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()));
                final Clipboard clipboard = reader.read();
                final EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()));
                editSession.setFastMode(true);

                final var operation = (new ClipboardHolder(clipboard)).createPaste(editSession).ignoreAirBlocks(false).to(to).build();
                Operations.complete(operation);

                editSession.close();
                editSession.close();
                reader.close();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
    }
}