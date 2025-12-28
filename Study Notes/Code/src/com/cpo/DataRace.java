package Code.src.com.cpo;

class Incr {

    private long x = 0;

    private long y = 0;

    public void incr() {
        x++;
        y++;
    }

    public void check() {
        if (y > x) {
            System.out.println("y > x");
        }
    }

    public void read(){
        System.out.println("x: " + x + ", y: " + y);
    }
}

public class DataRace {

    public static void main(String[] args) {

        Incr incrObj = new Incr();

        // single thread modifying shared state
        Thread t1 = new Thread(() -> {
            while (true) {
                incrObj.incr();
            }
        });

        // single thread checking shared state
        Thread t2 = new Thread(() -> {
            while (true) {
                incrObj.check();
            }
        });

        Thread t3 = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                incrObj.read();
            }
        });

        t1.start();
        t2.start();
        t3.start();
    }
}
