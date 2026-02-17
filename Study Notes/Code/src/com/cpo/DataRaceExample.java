package Code.src.com.cpo;

class DataRaceExample {
    static boolean ready = false;
    static int number = 0;

    public static void main(String[] args) {
        Thread writer = new Thread(() -> {
            number = 42;
            ready = true;
        });

        Thread reader = new Thread(() -> {
            while (!ready) {
                // busy wait
            }
            System.out.println(number);
        });

        writer.start();
        reader.start();
    }
}
