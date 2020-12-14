package plachess.engine;

import java.util.ArrayList;

public interface Rules {
    int BOARD_SIZE = 8;
    int ROW_WHITE = 0;
    int ROW_BLACK = 7;
    int DIR_WHITE = 1;
    int DIR_BLACK = -1;
    int COL_KING = 4;
    int COL_QUEEN = 3;
    int BORDER_KING = 7;
    int BORDER_QUEEN = 0;
    int DRAW_HALFMOVES_CLAIM = 50;
    int DRAW_HALFMOVES_NOCLAIM = 75;
    Color FIRST_TURN = Color.WHITE;

    static int getColorHomeRow(Color color) { return color == Color.WHITE ? ROW_WHITE : ROW_BLACK; }
    static int getColorDirection(Color color) { return color == Color.WHITE ? DIR_WHITE : DIR_BLACK; }

    static boolean isPawnMoved(Position pos, Color color) {
        return pos.y == getColorHomeRow(color) + getColorDirection(color);
    }

    static boolean isPromotion(Piece piece, Position posTo) {
        return piece.type == PieceType.PAWN && posTo.y == getColorHomeRow(piece.color.opposite());
    }

    static boolean isValidCastlingSide(PieceType side) {
        return side == PieceType.KING || side == PieceType.QUEEN;
    }

    static ArrayList<Position> getEnpassantInvolvedPositions(Position enpassant) {
        if(enpassant == null)
            return null;
        int dirY = enpassant.y < 4 ? 1 : -1;
        ArrayList<Position> result = new ArrayList<>();
        Position endangered = enpassant.add(0, dirY);
        result.add(endangered);
        for(int x=-1; x<2; ++x) {
            Position attacking = enpassant.add(1, dirY);
            if (attacking.isValid())
                result.add(attacking);
        }
        return result;
    }
}
