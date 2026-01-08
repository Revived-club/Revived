package club.revived.duels.game.kit;

import club.revived.duels.game.duels.KitType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * DuelKit
 *
 * @author yyuh
 * @since 04.01.26
 */
public record DuelKit(
        KitType type,
        Map<Integer, ItemStack> content
) {}
