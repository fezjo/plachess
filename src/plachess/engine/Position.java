package plachess.engine;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @Immutable
 */
public class Position {
    private static final int MAX_CACHE = 16;
    private static final ArrayList<Position> cachePool;

    static {
        cachePool = new ArrayList<>();
        for(int y=-MAX_CACHE; y <= MAX_CACHE; ++y) {
            for(int x=-MAX_CACHE; x <= MAX_CACHE; ++x) {
                cachePool.add(new Position(x, y));
            }
        }
    }

    public static Position getNew(int x, int y) {
//        return new Position(x, y);
        if(Math.max(Math.abs(y), Math.abs(x)) > MAX_CACHE)
            return new Position(x, y);
        return cachePool.get((y + MAX_CACHE) * (MAX_CACHE * 2 + 1) + (x + MAX_CACHE));
    }

    public final int x, y;

    private Position() { x = 0; y = 0; }
    private Position(int x, int y) { this.x = x; this.y = y; }
    private Position(Position p) { x = p.x; y = p.y; }

    public Position add(Position that) { return Position.getNew(this.x+that.x, this.y+that.y); }
    public Position add(int x, int y) { return Position.getNew(this.x+x, this.y+y); }
    public Position sub(Position that) { return Position.getNew(this.x-that.x, this.y-that.y); }
    public Position sub(int x, int y) { return Position.getNew(this.x-x, this.y-y); }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
//        return y * Rules.BOARD_SIZE + x;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || obj.getClass() != getClass()) return false;
        Position that = (Position)obj;
        return this.x == that.x && this.y == that.y;
    }

    public boolean equals(int x, int y) { return this.x == x && this.y == y; }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}
