package club.revived.commons.generic;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * ListUtils
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public final class ListUtils {

    /**
     * Split an array into two lists representing its first and second halves.
     *
     * The split index is computed as `array.length / 2`. The method returns an outer
     * list of two elements: the first list contains elements from index `0` (inclusive)
     * to `mid` (exclusive), and the second list contains elements from `mid` (inclusive)
     * to the end of the array. The outer list is unmodifiable; the two inner lists are mutable copies.
     *
     * @param array the source array to split
     * @return a list of two lists: the first half and the second half of the input array
     */
    @NotNull
    public static <T> List<List<T>> splitInHalf(final T[] array) {
        final int mid = array.length / 2;

        final List<T> list = Arrays.asList(array);

        final List<T> first = new ArrayList<>(list.subList(0, mid));
        final List<T> second = new ArrayList<>(list.subList(mid, list.size()));

        return List.of(first, second);
    }

    @NotNull
    public static <T> List<List<T>> splitInHalf(final Collection<? extends T> list) {
        final int mid = list.size() / 2;

        final List<T> newList = new ArrayList<>(list);

        final List<T> first = new ArrayList<>(newList.subList(0, mid));
        final List<T> second = new ArrayList<>(newList.subList(mid, newList.size()));

        return List.of(first, second);
    }
}