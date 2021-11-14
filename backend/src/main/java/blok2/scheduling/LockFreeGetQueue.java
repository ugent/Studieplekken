package blok2.scheduling;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A queue that is thread-safe in a lock-free manner. This is similar to the standard ConcurrentLinkedQueue, but
 * slightly adapted. Based on the algorithm of M. Michael and L.Scott.
 */
public class LockFreeGetQueue<T> {

    private final AtomicReference<Node<T>> head;
    private final AtomicReference<Node<T>> tail;
    
    public LockFreeGetQueue() {
        head = new AtomicReference<>(null);
        tail = new AtomicReference<>(null);
    }
    
    public void add(T element) {
        Node<T> node = new Node<>(element);
        Node<T> currentTail;
        do {
            currentTail = tail.get();
            node.previous = currentTail;
        } while(!tail.compareAndSet(currentTail, node));
        if (node.previous != null) {
            node.previous.next = node;
        }
        head.compareAndSet(null, node); // For inserting the first element.
    }
    
    public T get() {
        Node<T> currentHead;
        Node<T> nextNode;
        do {
            currentHead = head.get();
            if (currentHead != null) {
                nextNode = currentHead.next;
            } else {
                return null;
            }
        } while (!head.compareAndSet(currentHead, nextNode));
        return currentHead.value;
    }

    private static class Node<T> {
        // NOTE(ydndonck): These fields are volatile, because they should always
        // be updated in main memory (not cache). This is because different threads
        // could possibly be updating them at the same time. (But due to atomic operations this
        // will not be an issue.)
        public volatile T value;
        public volatile Node<T> previous;
        public volatile Node<T> next;

        public Node(T value) {
            this.value = value;
            this.previous = null;
            this.next = null;
        }
    }
    
}
