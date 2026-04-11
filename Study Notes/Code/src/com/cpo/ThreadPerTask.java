package Code.src.com.cpo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPerTask {

    public static void main(String[] args) {
        try(ExecutorService executorService = Executors.newCachedThreadPool()){
            while (true){
                executorService.submit(ThreadPerTask::blockingTask);
            }
        }
    }

    private static void blockingTask() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted.");
        }
        System.out.println("Task completed.");
    }
}
