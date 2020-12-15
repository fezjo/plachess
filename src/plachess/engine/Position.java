package plachess.engine;

import java.util.Objects;

/**
 * @Immutable
 */
public class Position {
    public final int x, y;

    Position() { x = 0; y = 0; }
    Position(int x, int y) { this.x = x; this.y = y; }
    Position(Position p) { x = p.x; y = p.y; }
    public boolean isValid() { return x >= 0 && x < Rules.BOARD_SIZE && y >= 0 && y < Rules.BOARD_SIZE; }
    public Position add(Position that) { return new Position(this.x+that.x, this.y+that.y); }
    public Position add(int x, int y) { return new Position(this.x+x, this.y+y); }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
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
