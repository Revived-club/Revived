package club.revived.lobby.game.parties;

import java.util.UUID;

/**
 * PartyRequest
 *
 * @author yyuh
 * @since 11.01.26
 */
public record PartyRequest(
        UUID sender,
        UUID receiver,
        Party party
) {}