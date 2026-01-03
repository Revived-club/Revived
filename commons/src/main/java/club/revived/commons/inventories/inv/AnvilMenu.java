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

    /**
     * Creates a new AnvilMenu and initializes its underlying AnvilGUI builder bound to the given plugin.
     *
     * @param plugin the plugin instance used to associate the AnvilGUI builder with the hosting plugin
     */
    private AnvilMenu(final @NotNull Plugin plugin) {
        this.builder = new AnvilGUI.Builder().plugin(plugin);
    }

    /**
     * Create a new AnvilMenu associated with the given plugin.
     *
     * @param plugin the plugin used to initialize the underlying AnvilGUI builder
     * @return the created AnvilMenu instance
     */
    @NotNull
    public static AnvilMenu of(final @NotNull Plugin plugin) {
        return new AnvilMenu(plugin);
    }

    /**
     * Sets the title displayed on the Anvil GUI.
     *
     * @param title the title text to display in the Anvil GUI; must not be null
     * @return the same AnvilMenu instance for method chaining
     */
    @NotNull
    public AnvilMenu setTitle(final @NotNull String title) {
        builder.title(title);
        return this;
    }

    /**
     * Set the initial text shown in the Anvil GUI input field.
     *
     * @param text the text to display in the input field; must not be null
     * @return this AnvilMenu instance for fluent chaining
     */
    @NotNull
    public AnvilMenu setText(final @NotNull String text) {
        builder.text(text);
        return this;
    }

    /**
     * Register a handler to be invoked when the anvil menu is closed.
     *
     * @param onClose the consumer to call with the Player who closed the menu
     * @return the current AnvilMenu instance for fluent chaining
     */
    @NotNull
    public AnvilMenu onClose(final @NotNull Consumer<Player> onClose) {
        builder.onClose(state -> onClose.accept(state.getPlayer()));
        return this;
    }

    /**
     * Register a click handler for the anvil menu.
     *
     * @param onClick a function invoked when a slot is clicked; receives the clicked slot index and the menu state snapshot and must return a list of response actions to perform
     * @return this AnvilMenu instance for fluent chaining
     */
    @NotNull
    public AnvilMenu onClick(final @NotNull BiFunction<Integer, AnvilGUI.StateSnapshot, List<AnvilGUI.ResponseAction>> onClick) {
        builder.onClick(onClick);
        return this;
    }

    /**
     * Prevents the menu from being closed by the player.
     *
     * @return the same {@code AnvilMenu} instance for fluent chaining
     */
    @NotNull
    public AnvilMenu preventClose() {
        builder.preventClose();
        return this;
    }

    /**
     * Opens this AnvilMenu for the specified player.
     *
     * @param player the player who will see the configured Anvil GUI
     */
    public void open(final @NotNull Player player) {
        builder.open(player);
    }
}