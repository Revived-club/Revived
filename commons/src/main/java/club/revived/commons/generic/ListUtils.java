package club.revived.commons.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ListUtils
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public final class ListUtils {

    public static <T> List<List<T>> splitInHalf(T[] array) {
        final int mid = array.length / 2;

        final List<T> list = Arrays.asList(array);

        final List<T> first = new ArrayList<>(list.subList(0, mid));
        final List<T> second = new ArrayList<>(list.subList(mid, list.size()));

        return List.of(first, second);
    }
}
