package Code.src.com.cpo;

import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumerThreeMonitors {

    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity = 5;

    // 3 monitors
    private final Object lock = new Object();         // protects queue
    private final Object notFullLock = new Object();  // producers wait
    private final Object notEmptyLock = new Object(); // consumers wait

    public void produce(int item) throws InterruptedException {

        // Wait until queue has space
        synchronized (notFullLock) {
            while (true) {
                synchronized (lock) {
                    if (queue.size() < capacity) {
                        break;
                    }
                }
                notFullLock.wait();
            }
        }

        // Add item (critical section)
        synchronized (lock) {
            queue.add(item);
            System.out.println("Produced: " + item);
        }

        // Notify consumers
        synchronized (notEmptyLock) {
            notEmptyLock.notify();
        }
    }

    public int consume() throws InterruptedException {
        int item;

        // Wait until queue has data
        synchronized (notEmptyLock) {
            while (true) {
                synchronized (lock) {
                    if (!queue.isEmpty()) {
                        break;
                    }
                }
                notEmptyLock.wait();
            }
        }

        // Remove item (critical section)
        synchronized (lock) {
            item = queue.remove();
            System.out.println("Consumed: " + item);
        }

        // Notify producers
        synchronized (notFullLock) {
            notFullLock.notify();
        }

        return item;
    }

    public static void main(String[] args) {
        ProducerConsumerThreeMonitors pc = new ProducerConsumerThreeMonitors();

        // Producer thread
        Thread producer = new Thread(() -> {
            int value = 0;
            try {
                while (true) {
                    pc.produce(value++);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    pc.consume();
                    Thread.sleep(800);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
    }
}