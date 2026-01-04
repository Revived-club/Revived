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
