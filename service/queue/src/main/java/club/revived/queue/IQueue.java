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

    /**
     * Enqueues the given item for processing.
     *
     * @param v the item to add to the queue
     */
    void push(final V v);

    /**
     * Removes the specified item(s) from the queue.
     *
     * @param v one or more items to remove from the queue
     */
    void pop(final V... v);

    /**
     * Removes any queued item(s) associated with the given key.
     *
     * @param k the key whose associated item(s) should be removed from the queue
     */
    void remove(final K k);

    /**
     * Begins processing items in the queue.
     * <p>
     * Initiates or schedules the queue's worker/task responsible for consuming and handling queued elements.
     */
    void startTask();

    /**
     * Retrieve the current items in the queue.
     *
     * @return a non-null List containing the items currently queued (may be empty)
     */
    @NotNull List<V> queued();
}