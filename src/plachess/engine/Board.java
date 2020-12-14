package plachess.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * board is always viewed from whites perspective
 * row and column numberings are from 0 to 7
 */
public interface Board {
    Board clone();
    boolean isOccupied(Position pos);
    default boolean isOccupied(int x, int y) { return isOccupied(new Position(x, y)); }

    /**
     * can throw exception if pos is not valid
     * @return Piece at position if occupied, otherwise null
     */
    Piece getPiece(Position pos);
    default Piece getPiece(int x, int y) { return getPiece(new Position(x, y)); }

    ArrayList<Piece> getAllPieces();

    /** @return all possible simple moves (travel, capture) by piece on given position */
    ArrayList<Move.MoveSimple> getSimpleMoves(Position pos);

    /** @return all possible simple moves (travel, capture) by all pieces of given color */
    ArrayList<Move.MoveSimple> getAllSimpleMoves(Color color);

    /** @return all pieces threatening given piece */
    ArrayList<Position> getThreatening(Piece piece);

    Board set(List<Pair<Position, Piece>> work);
}
