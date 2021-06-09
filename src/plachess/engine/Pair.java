package plachess.engine;

import java.util.Objects;

/** generic class for storing pair of objects */
public class Pair<T1, T2> {
    public T1 frst;
    public T2 scnd;

    public Pair() {}

    public Pair(T1 first, T2 second) {
        this.frst = first;
        this.scnd = second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(frst, scnd);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || obj.getClass() != getClass()) return false;
        Pair<Object, Object> that = (Pair<Object, Object>)obj;
        return this.frst.equals(that.frst) && this.scnd.equals(that.scnd);
    }

    public boolean equals(T1 first, T2 second) { return frst.equals(first) && scnd.equals(second); }

    @Override
    public String toString() {
        return String.format("(%s, %s)", frst.toString(), scnd.toString());
    }
}
