package Code.src.com.cpo;

import Code.src.com.extras.Utils;

import java.util.concurrent.CompletableFuture;

import static Code.src.com.extras.Utils.blockForAndReturn;

public class FutureTaskVSCompletableFuture {

    private static void futureTask() {
        int x = blockForAndReturn(5000, 42);
        use(x);
        System.out.println("Done with FutureTask");
    }

    private static void completableFuture() {
        CompletableFuture.supplyAsync(() -> blockForAndReturn(5000, 42))
                .thenAccept(FutureTaskVSCompletableFuture::use);
        System.out.println("Done with CompletableFuture");
    }

    private static void use(int x) {
        System.out.println("Result is : " + x);
    }

    public static void main(String[] args) {
        // FutureTask
        long start = System.currentTimeMillis();
        futureTask();
        long end = System.currentTimeMillis();
        System.out.println("Total time for FutureTask: " + (end - start) + " ms");

        // CompletableFuture
        start = System.currentTimeMillis();
        completableFuture();
        end = System.currentTimeMillis();
        System.out.println("Total time for CompletableFuture: " + (end - start) + " ms");
    }

}
