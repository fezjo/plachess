package plachess.engine;

/** class for measuring time intervals */
public class Stopwatch {
    public String message;
    public long tStart, tFinish;

    public Stopwatch() {
        start();
    }

    public Stopwatch(String message) {
        this.message = message;
        start();
    }

    public long start() {
        return tStart = System.currentTimeMillis();
    }

    public long finish() {
        return tFinish = System.currentTimeMillis();
    }

    public long finish(boolean print) {
        long result = finish();
        if(print)
            print();
        return result;
    }

    public long print() {
        long duration = tFinish - tStart;
        System.out.printf(message, duration);
        return duration;
    }
}
