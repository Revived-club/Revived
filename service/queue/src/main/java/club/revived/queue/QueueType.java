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

    QueueType(int teamSize) {
        this.teamSize = teamSize;
    }

    public int teamSize() {
        return teamSize;
    }

    public int totalPlayers() {
        return teamSize * 2;
    }
}

