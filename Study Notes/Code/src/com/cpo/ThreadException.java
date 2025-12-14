package Code.src.com.cpo;

public class ThreadException {

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            throw new RuntimeException("Test exception");
        });
        t1.setUncaughtExceptionHandler((t, e) -> {
            System.out.println("A critical exception occurred in thread " + t.getName() + ". Exception : " + e.getMessage());
        });
        t1.start();
    }
}