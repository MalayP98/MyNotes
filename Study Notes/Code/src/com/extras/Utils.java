package Code.src.com.extras;

public final class Utils {

    public static void blockFor(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static  <T> T blockForAndReturn(int milliseconds, T value) {
        blockFor(milliseconds);
        return value;
    }
}
