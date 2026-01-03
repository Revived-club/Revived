package club.revived.commons.inventories.inv;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class AnvilMenu {

    @NotNull
    private final AnvilGUI.Builder builder;

    private AnvilMenu(final @NotNull Plugin plugin) {
        this.builder = new AnvilGUI.Builder().plugin(plugin);
    }

    @NotNull
    public static AnvilMenu of(final @NotNull Plugin plugin) {
        return new AnvilMenu(plugin);
    }

    @NotNull
    public AnvilMenu setTitle(final @NotNull String title) {
        builder.title(title);
        return this;
    }

    @NotNull
    public AnvilMenu setText(final @NotNull String text) {
        builder.text(text);
        return this;
    }

    @NotNull
    public AnvilMenu onClose(final @NotNull Consumer<Player> onClose) {
        builder.onClose(state -> onClose.accept(state.getPlayer()));
        return this;
    }

    @NotNull
    public AnvilMenu onClick(final @NotNull BiFunction<Integer, AnvilGUI.StateSnapshot, List<AnvilGUI.ResponseAction>> onClick) {
        builder.onClick(onClick);
        return this;
    }

    @NotNull
    public AnvilMenu preventClose() {
        builder.preventClose();
        return this;
    }

    public void open(final @NotNull Player player) {
        builder.open(player);
    }
}
