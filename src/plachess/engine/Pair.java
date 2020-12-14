package plachess.engine;

public class Pair<T1, T2> {
    public T1 frst;
    public T2 scnd;

    public Pair() {}

    public Pair(T1 first, T2 second) {
        this.frst = first;
        this.scnd = second;
    }
}
