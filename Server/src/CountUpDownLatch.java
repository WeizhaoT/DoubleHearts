
import java.util.concurrent.CountDownLatch;

/**
 * 
 * 
 * 
 * @see http://www.java2s.com/Tutorial/Java/0120__Development/CountUpDownLatch.htm
 */
public class CountUpDownLatch {
    private CountDownLatch latch;
    private final int origCount;
    private int count;

    /**
     * 
     * @param count
     */
    public CountUpDownLatch(final int count) {
        latch = new CountDownLatch(1);
        this.count = count;
        origCount = count;
    }

    public void await() throws InterruptedException {
        if (count == 0) {
            return;
        }
        latch.await();
    }

    public void countDown() {
        if ((--count) == 0) {
            latch.countDown();
        }
    }

    public int getCount() {
        return count;
    }

    public void countUp() {
        if (latch.getCount() == 0) {
            latch = new CountDownLatch(1);
        }
        count++;
    }

    public void setCount(final int count) {
        if (count == 0) {
            if (latch.getCount() != 0) {
                latch.countDown();
            }
        } else if (latch.getCount() == 0) {
            latch = new CountDownLatch(1);
        }
        this.count = count;
    }

    public void reset() {
        latch = new CountDownLatch(1);
        count = origCount;
    }
}