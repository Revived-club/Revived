package club.revived.lobby.game.kit;

import club.revived.lobby.game.duel.KitType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * KitHolderTemplate
 *
 * @author yyuh
 * @since 11.01.26
 */
public record KitTemplate(
        KitType kitType,
        Map<Integer, ItemStack> content
) {}
