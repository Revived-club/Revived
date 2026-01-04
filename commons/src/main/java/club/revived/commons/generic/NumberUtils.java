package club.revived.commons.generic;

/**
 * NumberUtils
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class NumberUtils {

    /**
     * Determines whether the given string represents a valid 32-bit signed integer.
     *
     * @param a the string to test; may be null (treated as not an integer)
     * @return {@code true} if {@code a} can be parsed as an integer, {@code false} otherwise
     */
    public static boolean isInteger(final String a) {
        try {
            Integer.parseInt(a);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}