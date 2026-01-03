package club.revived.lobby.game.kit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record Kit(
        UUID owner,
        int id,
        String name,
        Map<Integer, ItemStack> content,
        boolean selected
) {

    /**
     * Applies this kit to the given player by setting the player's inventory to the kit contents,
     * restoring hunger and saturation to full, clearing active potion effects, and notifying the player.
     *
     * @param player the player to receive the kit
     */
    public void load(final Player player) {
        player.getInventory().setContents(content.values().toArray(new ItemStack[0]));
        player.setFoodLevel(20);
        player.getActivePotionEffects().clear();
        player.setSaturation(20);
        player.sendRichMessage(String.format("<green>Kit %d loaded", this.id));

        // TODO: Implement last used kits
    }
}