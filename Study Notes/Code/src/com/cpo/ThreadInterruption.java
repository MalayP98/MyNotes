package Code.src.com.cpo;

import java.math.BigInteger;

public class ThreadInterruption {

    /**
     * Call where interrupt is already taken care of. Calling {@code thread.interrupt()} is enough
     */
    static class SleepingThread implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted.");
            }
        }
    }

    /**
     * Interrupt is taken care of by default, need to add {@code Thread.currentThread().isInterrupted()} condition
     * to come out of this thread.
     */
    static class LongRunningTask implements Runnable {

        private BigInteger base;

        private int power;

        public LongRunningTask(BigInteger base, int power) {
            this.base = base;
            this.power = power;
        }

        private BigInteger pow(BigInteger base, int power) {
            BigInteger res = BigInteger.ONE;
            for (int i = 0; i < power; i++) {
                // Add this to take care of interruption
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread was interrupted, returning 0");
                    return BigInteger.ZERO;
                }
                res = res.multiply(base);
            }
            return res;
        }

        @Override
        public void run() {
            System.out.println("Result : " + pow(base, power).toString());
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new SleepingThread());
        t1.start();
        t1.interrupt();

        Thread t2 = new Thread(new LongRunningTask(new BigInteger("200"), 100000));
        t2.start();
        t2.interrupt();

        // Deamon thread example
        t2 = new Thread(new LongRunningTask(new BigInteger("200"), 100000));
        t2.setDaemon(true);
        t2.start();
//        t2.interrupt();
    }
}
