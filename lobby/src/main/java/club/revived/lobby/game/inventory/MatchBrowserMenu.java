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

    /**
     * Creates and opens a "Match Browser" inventory populated with the provided matches for the given player.
     *
     * @param cachedDuels a list of Game instances to display as menu items
     * @param player the player who will be shown the menu
     */
    public MatchBrowserMenu(
            final List<Game> cachedDuels,
            final Player player
    ) {
        ListMenu.of("Match Browser")
                .addItems(this.matchItems(cachedDuels))
                .open(player);
    }

    /**
     * Builds menu item representations for the provided matches.
     *
     * Each item uses the match's kit material and beautified kit name, and includes lore lines for
     * the match id, kit name, game state, max rounds, and team composition.
     *
     * @param matches the list of matches to convert into menu items
     * @return a list of ItemBuilder objects representing the given matches
     */
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