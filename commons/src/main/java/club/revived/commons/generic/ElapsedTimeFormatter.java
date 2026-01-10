package club.revived.commons.generic;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * ElapsedTimeFormatter
 *
 * @author yyuh
 * @since 10.01.26
 */
public final class ElapsedTimeFormatter {

    private long startTime;

    /**
     * Creates a new ElapsedTimeFormatter and initializes its start time to the current system time.
     */
    public ElapsedTimeFormatter() {
        start();
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    @NotNull
    public String getElapsedTime() {
        final long elapsedMillis = System.currentTimeMillis() - startTime;
        return formatMillis(elapsedMillis);
    }

    public long getElapsedTimeMillis() {
        return System.currentTimeMillis() - startTime;
    }

    @NotNull
    public static String formatMillis(final long millis) {
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes);

        final StringBuilder sb = new StringBuilder();
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");

        return sb.toString();
    }
}
