package Code.src.com.cpo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static Code.src.com.extras.Utils.blockFor;

public class NonBlockingIO {

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> blockFor(5000));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long end = System.currentTimeMillis();
        System.out.println("By Main thread : " + (end - start) + " ms");


    }

}
