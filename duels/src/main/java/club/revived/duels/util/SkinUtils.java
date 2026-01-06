package club.revived.duels.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SkinUtils {

    @NotNull
    public static String getSkin(final Player player) {
        final CraftPlayer craftPlayer = (CraftPlayer) player;
        final GameProfile profile = craftPlayer.getProfile();

        final Property textures = profile.properties().get("textures").iterator().next();
        return textures.value();
    }

    @Nullable
    public static String getSignature(final Player player) {
        final CraftPlayer craftPlayer = (CraftPlayer) player;
        final GameProfile profile = craftPlayer.getProfile();

        final Property textures = profile.properties().get("textures").iterator().next();
        return textures.signature();
    }

}
