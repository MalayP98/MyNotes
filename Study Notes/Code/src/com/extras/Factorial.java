package Code.src.com.extras;

import java.math.BigInteger;

/**
 * Used to mimic long tasks
 */
public class Factorial extends Thread {

    private final BigInteger element;

    private BigInteger res = BigInteger.ONE;

    public Factorial(BigInteger element) {
        this.element = element;
    }

    private void calc() {
        for (BigInteger i = BigInteger.ONE; i.compareTo(element) != 0; i = i.add(BigInteger.ONE)) {
            res = res.multiply(i);
        }
    }

    @Override
    public void run(){
        calc();
    }

    public BigInteger getResult(){
        return res;
    }
}
