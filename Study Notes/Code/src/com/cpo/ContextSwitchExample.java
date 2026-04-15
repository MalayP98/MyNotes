package Code.src.com.cpo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ContextSwitchExample {

    private static final int REQ = 10000;

    public static void main(String[] args) throws InterruptedException {
        ContextSwitchExample example = new ContextSwitchExample();

        // Simulation 1
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < REQ; i++) {
            executorService.submit(example::blockingTaskV1);
        }
        executorService.shutdown();
        try{
            boolean terminated = executorService.awaitTermination(1, TimeUnit.MINUTES);
            if (!terminated) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Total time for Simulation 1: " + (endTime - startTime) + " ms");

        Thread.sleep(2000);

        // Simulation 2
        executorService = Executors.newFixedThreadPool(1000);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < REQ; i++) {
            executorService.submit(example::blockingTaskV2);
        }
        executorService.shutdown();
        try{
            boolean terminated = executorService.awaitTermination(1, TimeUnit.MINUTES);
            if (!terminated) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        endTime = System.currentTimeMillis();
        System.out.println("Total time for Simulation 2: " + (endTime - startTime) + " ms");

    }

    private void blockFor(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void blockingTaskV1(){
        System.out.println("Blocking Task V1: Starting on " + Thread.currentThread().getName());
        blockFor(1000);
        System.out.println("Blocking Task V1: Finished on " + Thread.currentThread().getName());
    }

    private void blockingTaskV2(){
        System.out.println("Blocking Task V2: Starting on " + Thread.currentThread().getName());
        for(int i=0; i<100; i++){
            blockFor(10); // Simulate work with multiple short blocks
        }
        System.out.println("Blocking Task V2: Finished on " + Thread.currentThread().getName());
    }

}
