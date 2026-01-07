package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.ListMenu;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.duel.Game;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * MatchBrowserMenu
 *
 * @author yyuh - DL
 * @since 1/7/26
 */
public final class MatchBrowserMenu {

    public MatchBrowserMenu(
            final List<Game> cachedDuels,
            final Player player
    ) {
        ListMenu.of("Match Browser")
                .addItems(this.matchItems(cachedDuels))
                .open(player);
    }

    @NotNull
    private List<ItemBuilder> matchItems(final List<Game> matches) {
        final var itemBuilders = new ArrayList<ItemBuilder>();

        for (final var match : matches) {
            itemBuilders.add(ItemBuilder.item(match.kitType().getMaterial())
                    .name(String.format("<white>%s", match.kitType().getBeautifiedName()))
                    .lore(
                            ColorUtils.parse(String.format("<dark_gray>%s", match.id())),
                            ColorUtils.parse(match.kitType().name()),
                            ColorUtils.parse(match.gameState().toString()),
                            ColorUtils.parse("Max Rounds " + match.rounds()),
                            ColorUtils.parse(match.blueTeam() + " - " + match.redTeam())
                    )
            );
        }

        return itemBuilders;
    }
}
