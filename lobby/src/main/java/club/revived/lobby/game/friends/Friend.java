package club.revived.lobby.game.friends;

import java.util.UUID;

/**
 * Friend
 *
 * @author yyuh
 * @since 15.01.26
 */
public record Friend(
        UUID uuid,
        long friendsSince
) {
}
