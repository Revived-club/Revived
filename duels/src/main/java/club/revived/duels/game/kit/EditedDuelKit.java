package club.revived.duels.game.kit;

import club.revived.duels.game.duels.KitType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

/**
 * EditedDuelKit
 *
 * @author yyuh
 * @since 04.01.26
 */
public record EditedDuelKit(
        UUID uuid,
        Map<Integer, ItemStack> content,
        KitType type
) {}
