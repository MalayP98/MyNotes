package Code.src.com.cpo;

class Counter {
    private int counter = 0;

    public void increment() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }
}

public class RaceCondition {

    public static void main(String[] args) {

        Counter sharedCounter = new Counter();

        /*
        LocalCounter is not being shared between threads. Is part of thread's local memory/stack.
        But as it references sharedCounter, both threads are incrementing the same counter variable.
        This can lead to race conditions where both threads read, increment, and write back the counter value simultaneously,
        resulting in lost updates and an incorrect final counter value.
         */

        Thread t1 = new Thread(() -> {
            Counter localCounter = sharedCounter;
            for (int i = 0; i < 1000; i++) {
                localCounter.increment();
            }
        });

        Thread t2 = new Thread(() -> {
            Counter localCounter = sharedCounter;
            for (int i = 0; i < 1000; i++) {
                localCounter.increment();
            }
        });

        System.out.println("Counter before starting threads: " + sharedCounter.getCounter());
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Counter after threads have finished: " + sharedCounter.getCounter());
    }
}
