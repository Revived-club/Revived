package club.revived.lobby;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Lobby extends JavaPlugin {

    private static Lobby instance;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onDisable() {
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    public static Lobby getInstance() {
        return instance;
    }
}
