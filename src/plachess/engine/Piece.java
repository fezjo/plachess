package plachess.engine;

public class Piece {
    public final Color color;
    public final Position pos;
    public final PieceType type;

    public static Piece empty() { return null; }
    public static boolean isEmpty(Piece p) { return p == null || p.type == PieceType.EMPTY; }

    public Piece(int x, int y, Color color, PieceType type) {
        this(new Position(x, y), color, type);
    }

    public Piece(Position pos, Color color, PieceType type) {
        this.pos = pos;
        this.color = color;
        this.type = type;
    }

    public Piece setPos(Position pos) { return new Piece(pos, this.color, this.type); }
    public Piece setPosition(int x, int y) { return new Piece(x, y, this.color, this.type); }

    @Override
    public String toString() {
        return String.format("(%s %s at %s)", color.name(), type.name(), pos);
    }
}
