package club.revived.lobby.game.friends;

import java.util.UUID;

/**
 * FriendRequest
 *
 * @author yyuh
 * @since 15.01.26
 */
public record FriendRequest(
        UUID sender,
        UUID receiver
) {
}
