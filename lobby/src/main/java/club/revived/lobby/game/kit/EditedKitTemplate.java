package club.revived.lobby.game.kit;

import club.revived.lobby.game.duel.KitType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * EditedKitTemplate
 *
 * @author yyuh
 * @since 17.01.26
 */
public record EditedKitTemplate(
        UUID uuid,
        KitType kitType,
        Map<Integer, ItemStack> content
) {

    @NotNull
    public static EditedKitTemplate fromTemplate(
            final UUID uuid,
            final KitTemplate kitTemplate
    ){
        return new EditedKitTemplate(
                uuid,
                kitTemplate.kitType(),
                kitTemplate.content()
        );
    }
}
