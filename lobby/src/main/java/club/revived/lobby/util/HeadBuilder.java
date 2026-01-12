package club.revived.lobby.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * This is an interesting Class
 *
 * <a href="https://gist.github.com/Kamilkime/a295a2d4aaae189795f6efcd6a7cb0e9">Source</a>
 * @author Kamilkime
 * @since 12.01.26
 */
public final class HeadBuilder {

    private static Constructor<?> gameProfileConstructor;
    private static Constructor<?> propertyConstructor;
    private static Method propertyMapPut;
    private static Method metaSetProfile;
    private static Field propertiesField;

    /**
     * Tries to set specific texture data to a given SkullMeta
     *
     * @param skullMeta The modified item meta
     * @param value Base64 value of the skin
     * @param signature Base64 signature for the skin or empty String for no signature
     */
    public static void setSkullTexture(@NotNull final SkullMeta skullMeta, @NotNull final String value, @NotNull final String signature) {
        if (propertiesField == null) {
            init(skullMeta);
        }

        try {
            final Object gameProfile = gameProfileConstructor.newInstance(UUID.randomUUID(), null);
            final Object skinProperty = propertyConstructor.newInstance("textures", value, signature.isEmpty() ? null : signature);

            propertyMapPut.invoke(propertiesField.get(gameProfile), "textures", skinProperty);
            metaSetProfile.invoke(skullMeta, gameProfile);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Tries to set specific texture data to a given ItemStack
     *
     * @param itemStack The modified ItemStack
     * @param value Base64 value of the skin
     * @param signature Base64 signature for the skin or empty String for no signature
     */
    public static ItemStack setSkullTexture(@NotNull final ItemStack itemStack, @NotNull final String value, @NotNull final String signature) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof SkullMeta)) {
            return itemStack;
        }

        setSkullTexture((SkullMeta) itemMeta, value, signature);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /**
     * Tries to initialize reflections for item texture modification
     *
     * @param skullMeta An instance of SkullMeta, to be used in reflections
     */
    private static void init(final SkullMeta skullMeta) {
        try {
            final Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            final Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            final Class<?> propertyMapClass = Class.forName("com.mojang.authlib.properties.PropertyMap");

            gameProfileConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
            propertyConstructor = propertyClass.getConstructor(String.class, String.class, String.class);
            propertyMapPut = propertyMapClass.getMethod("put", Object.class, Object.class);
            metaSetProfile = skullMeta.getClass().getDeclaredMethod("setProfile", gameProfileClass);
            propertiesField = gameProfileClass.getDeclaredField("properties");

            propertyMapPut.setAccessible(true);
            metaSetProfile.setAccessible(true);
            propertiesField.setAccessible(true);
        } catch (final NoSuchFieldException | ClassNotFoundException | NoSuchMethodException exception) {
            exception.printStackTrace();
        }
    }

    private HeadBuilder() {}

}