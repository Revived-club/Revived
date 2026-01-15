package club.revived.lobby.game.friends;

import club.revived.lobby.game.duel.KitType;

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
