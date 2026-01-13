package club.revived.commons.inventories.util;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
        final var json = "{\"SKIN\":{\"url\":\"%s\"}}".formatted(url);
        final var base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        head.setData(
                DataComponentTypes.PROFILE,
                ResolvableProfile.resolvableProfile().addProperty(new ProfileProperty("textures", base64))
        );

        return head;
    }
}