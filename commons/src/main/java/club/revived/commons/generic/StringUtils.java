package club.revived.commons.generic;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * StringUtils
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class StringUtils {

    @NotNull
    public static String generateId(final String prefix) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; ++i) {
            final var chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";
            int randomIndex = random.nextInt(chars.length());
            char randomChar = chars.charAt(randomIndex);

            builder.append(randomChar);
        }
        return builder.append(prefix).toString();
    }
}
