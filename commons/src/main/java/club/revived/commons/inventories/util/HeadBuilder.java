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
        final var head = ItemStack.of(Material.PLAYER_HEAD);
        final var json = "{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}".formatted(url);
        final var base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        final var profile = ResolvableProfile.resolvableProfile()
                .uuid(UUID.randomUUID())
                .addProperty(new ProfileProperty("textures", base64));

        head.setData(DataComponentTypes.PROFILE, profile.build());
        return head;
    }
}