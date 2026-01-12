package club.revived.lobby.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * HeadBuilder
 *
 * @author yyuh
 * @since 12.01.26
 */
public final class HeadBuilder {

    private final ItemStack item;
    private final SkullMeta meta;

    public HeadBuilder() {
        this.item = new ItemStack(Material.PLAYER_HEAD, 1);
        this.meta = (SkullMeta) item.getItemMeta();
    }

    @NotNull
    public static HeadBuilder of() {
        return new HeadBuilder();
    }

    @NotNull
    public HeadBuilder owner(final String name) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        meta.setOwningPlayer(player);
        return this;
    }

    @NotNull
    public HeadBuilder owner(final UUID uuid) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        meta.setOwningPlayer(player);
        return this;
    }

    @NotNull
    public HeadBuilder texture(final String base64) {
        final PropertyMap properties = new PropertyMap(PropertyMap.EMPTY);
        properties.put("textures", new Property("textures", base64));

        final GameProfile profile = new GameProfile(UUID.randomUUID(), "", properties);

        try {
            final Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to set skull texture", e);
        }

        return this;
    }

    @NotNull
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
