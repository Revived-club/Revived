package club.revived.commons;

/**
 * NumberUtils
 *
 * @author yyuh
 * @since 03.01.26
 */
public class NumberUtils {

    public static boolean isInteger(final String a) {
        try {
            Integer.parseInt(a);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
