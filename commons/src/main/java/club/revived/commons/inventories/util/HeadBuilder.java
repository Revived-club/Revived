package club.revived.commons.inventories.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 12.01.26
 */
public final class HeadBuilder {

    @NotNull
    public static ItemStack customHead(final String url) {
        try {
            final UUID uuid = UUID.randomUUID();
            final PlayerProfile profile = Bukkit.createProfile(uuid);

            PlayerTextures textures = profile.getTextures();
            profile.setTextures(textures);

            final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setPlayerProfile(profile);
            textures.setSkin(URI.create(url).toURL());

            head.setItemMeta(skullMeta);

            return head;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}