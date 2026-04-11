package Code.src.com.cpo;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock-free Stack implementation using AtomicReference (CAS).
 *
 * Key idea:
 * - Stack is represented as a linked list
 * - Head pointer is updated using Compare-And-Set (CAS)
 * - No synchronized / locks → non-blocking
 */
public class LockFreeStack<T> {

    /**
     * Node representing each element in the stack
     */
    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    /**
     * Head pointer of the stack
     * AtomicReference ensures atomic updates using CAS
     */
    private final AtomicReference<Node<T>> head = new AtomicReference<>(null);

    /**
     * Push operation (add element to top of stack)
     */
    public void push(T value) {
        Node<T> newNode = new Node<>(value);

        while (true) {
            // Step 1: Read current head
            Node<T> currentHead = head.get();

            // Step 2: Point new node to current head
            newNode.next = currentHead;

            // Step 3: Try to update head using CAS
            // If head is still currentHead → update to newNode
            if (head.compareAndSet(currentHead, newNode)) {
                // Success → element pushed
                return;
            }

            // If CAS fails:
            // Another thread modified head → retry
        }
    }

    /**
     * Pop operation (remove element from top of stack)
     */
    public T pop() {
        while (true) {
            // Step 1: Read current head
            Node<T> currentHead = head.get();

            // Step 2: Check if stack is empty
            if (currentHead == null) {
                return null; // or throw exception
            }

            // Step 3: Next node becomes new head
            Node<T> newHead = currentHead.next;

            // Step 4: Try to update head using CAS
            if (head.compareAndSet(currentHead, newHead)) {
                // Success → return value
                return currentHead.value;
            }

            // If CAS fails:
            // Another thread interfered → retry
        }
    }

    /**
     * Peek operation (read top element without removing)
     */
    public T peek() {
        Node<T> currentHead = head.get();
        return currentHead != null ? currentHead.value : null;
    }
}