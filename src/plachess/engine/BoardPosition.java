package plachess.engine;

import java.util.*;
import java.util.stream.Stream;

public interface BoardPosition {
    /** @return deep copy of board */
    Board getBoard();
    Color getTurnColor();
    /** @return copy of castling array (WK, WQ, BK, BQ) */
    boolean[] getCastling();
    Position getEnpassant();
    int getHalfMoveClock();
    int getFullMoveClock();

    Piece getPiece(Position pos);
    Piece getPiece(int x, int y);
    PieceType getPieceType(Position pos);
    PieceType getPieceType(int x, int y);
    Color getPieceColor(Position pos);
    Color getPieceColor(int x, int y);

    boolean canCastle(Color color, PieceType side);

    /**
     * @param side KING or QUEEN side
     * @return index to castling array corresponding to information provided
     */
    static int castlingArrayIndex(Color color, PieceType side) {
        if(!Rules.isValidCastlingSide(side)) throw new IllegalArgumentException("Invalid castling side");
        return (color == Color.WHITE ? 0 : 2) + (side == PieceType.KING ? 0 : 1);
    }

    ArrayList<BoardPosition> getMoves();
    boolean isKingValid();
    boolean isDeadPosition();
    boolean canCallDraw();
    boolean isCheck(Color color);

    // isDraw, isCheckMate
}
