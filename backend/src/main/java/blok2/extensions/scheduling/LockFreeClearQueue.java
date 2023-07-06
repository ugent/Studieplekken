package blok2.extensions.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Queue that is thread-safe in a lock-free manner. This is similar to the standard ConcurrentLinkedQueue, but
 * slightly adapted. Based on the algorithm of M. Michael and L.Scott.
 */
public class LockFreeClearQueue<T> {
    
    private final AtomicReference<Node<T>> tail;
    
    public LockFreeClearQueue() {
        tail = new AtomicReference<>(new Node<>(null, 0));
    }

    /**
     * Adds an element to the queue and returns the amount of elements currently stored
     * in the queue.
     * @return : The amount of elements currently stored in the queue.
     */
    public int add(T element) {
        Node<T> node = new Node<>(element, 0);
        Node<T> currentTail;
        do {
            currentTail = tail.get();
            node.previous = currentTail;
            node.index = currentTail.index + 1;
        } while (!tail.compareAndSet(currentTail, node));
        return node.index;
    }

    /**
     * Clears the queue and returns all elements that were contained in it.
     * @return
     */
    public List<T> clear() {
        Node<T> node = new Node<>(null, 0);
        Node<T> currentTail;
        do {
            currentTail = tail.get();
        } while(!tail.compareAndSet(currentTail, node));
        List<T> list = new ArrayList<>();
        if (currentTail == null) {
            return list;
        }
        do {
            if (currentTail.value != null) {
                list.add(currentTail.value);
            }
            currentTail = currentTail.previous;
        } while (currentTail != null);
        return list;
    }
    
    
    private static class Node<T> {
        // NOTE(ydndonck): These fields are volatile, because they should always
        // be updated in main memory (not cache). This is because different threads
        // could possibly be updating them at the same time. (But due to atomic operations this
        // will not be an issue.)
        public volatile T value;
        public volatile Node<T> previous;
        public volatile int index;
        
        public Node(T value) {
            this.value = value;
            this.previous = null;
            this.index = 0;
        }
        
        public Node(T value, int index) {
            this.value = value;
            this.previous = null;
            this.index = index;
        }
    }
}
