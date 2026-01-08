package club.revived.queue;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * IQueue
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public interface IQueue<K, V> {

    void push(final V v);
    void pop(final V... v);
    void remove(final K k);
    void startTask();
    @NotNull List<V> queued();
}
