package plachess.engine;

import java.util.Objects;

/**
 * @Immutable
 * container class for all information about chess piece
 */
public class Piece {
    public final Color color;
    public final Position pos;
    public final PieceType type;

    public static Piece empty() { return null; }
    public static boolean isEmpty(Piece p) { return p == null || p.type == PieceType.EMPTY; }

    public Piece(int x, int y, Color color, PieceType type) {
        this(Position.getNew(x, y), color, type);
    }

    public Piece(Position pos, Color color, PieceType type) {
        this.pos = pos;
        this.color = color;
        this.type = type;
    }

    public Piece setPos(Position pos) { return new Piece(pos, this.color, this.type); }
    public Piece setPos(int x, int y) { return new Piece(x, y, this.color, this.type); }

    @Override
    public int hashCode() {
        return Objects.hash(pos, color, type);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || obj.getClass() != getClass()) return false;
        Piece that = (Piece)obj;
        return this.color == that.color && this.type == that.type && this.pos.equals(that.pos);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return this;
    }

    @Override
    public String toString() {
        return String.format("(%s %s at %s)", color.name(), type.name(), pos);
    }
}
