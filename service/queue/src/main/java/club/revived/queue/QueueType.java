package club.revived.queue;

/**
 * QueueType
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public enum QueueType {

    SOLO(1),
    DUO(2),
    TRIO(3);

    private final int teamSize;

    /**
     * Creates a QueueType with the given number of players per team.
     *
     * @param teamSize the number of players in a single team for this queue type
     */
    QueueType(int teamSize) {
        this.teamSize = teamSize;
    }

    /**
     * Gets the team size for this queue type.
     *
     * @return the number of players per team for this queue type
     */
    public int teamSize() {
        return teamSize;
    }

    /**
     * Computes the total number of players in a match (both teams).
     *
     * @return `teamSize` multiplied by 2, i.e., the total number of players for this queue type.
     */
    public int totalPlayers() {
        return teamSize * 2;
    }
}
