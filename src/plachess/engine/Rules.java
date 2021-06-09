package plachess.engine;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** collections of constants and chess helper methods */
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

    static boolean isPositionValid(Position pos) {
        return pos.x >= 0 && pos.x < Rules.BOARD_SIZE
            && pos.y >= 0 && pos.y < Rules.BOARD_SIZE;
    }

    static int getColorHomeRow(Color color) { return color == Color.WHITE ? ROW_WHITE : ROW_BLACK; }
    static int getColorDirection(Color color) { return color == Color.WHITE ? DIR_WHITE : DIR_BLACK; }

    static boolean isPawnMoved(Position pos, Color color) {
        return pos.y != getColorHomeRow(color) + getColorDirection(color);
    }

    static boolean isPromotion(Piece piece, Position posTo) {
        return piece.type == PieceType.PAWN && posTo.y == getColorHomeRow(piece.color.opposite());
    }

    static boolean isValidCastlingSide(PieceType side) {
        return side == PieceType.KING || side == PieceType.QUEEN;
    }

    /** returns list of positions important for evaluation of enpassant move
     * order being [removedPawn, attackingAfter, attackingBeforeLeft, attackingBeforeRight
     */
    static ArrayList<Position> getEnpassantInvolvedPositions(Position enpassant) {
        if(enpassant == null)
            return null;
        int dirY = enpassant.y < 4 ? 1 : -1;
        ArrayList<Position> result = new ArrayList<>();
        result.add(enpassant.add(0, dirY)); // victim pawn
        result.add(enpassant); // where attacker will end up
        for(int x = -1; x < 2; x += 2) { // both possible attackers
            Position attacking = enpassant.add(x, dirY);
            if (isPositionValid(attacking))
                result.add(attacking);
        }
        return result;
    }


    Map<Pair<Color, PieceType>, Character> piece2char = new HashMap<>(Stream.of(
            new Pair<>(new Pair<>(Color.WHITE, PieceType.PAWN), 'P'),
            new Pair<>(new Pair<>(Color.WHITE, PieceType.KNIGHT), 'N'),
            new Pair<>(new Pair<>(Color.WHITE, PieceType.BISHOP), 'B'),
            new Pair<>(new Pair<>(Color.WHITE, PieceType.ROOK), 'R'),
            new Pair<>(new Pair<>(Color.WHITE, PieceType.QUEEN), 'Q'),
            new Pair<>(new Pair<>(Color.WHITE, PieceType.KING), 'K'),
            new Pair<>(new Pair<>(Color.BLACK, PieceType.PAWN), 'p'),
            new Pair<>(new Pair<>(Color.BLACK, PieceType.KNIGHT), 'n'),
            new Pair<>(new Pair<>(Color.BLACK, PieceType.BISHOP), 'b'),
            new Pair<>(new Pair<>(Color.BLACK, PieceType.ROOK), 'r'),
            new Pair<>(new Pair<>(Color.BLACK, PieceType.QUEEN), 'q'),
            new Pair<>(new Pair<>(Color.BLACK, PieceType.KING), 'k')).collect(
            Collectors.toMap(p -> p.frst, p -> p.scnd)));

    Map<Character, Piece> char2piece = new HashMap<>(piece2char.
            entrySet().stream().collect(
            Collectors.toMap(
                    Map.Entry::getValue,
                    e -> new Piece(null, e.getKey().frst,  e.getKey().scnd)
            )));

    List<PieceType> PAWN_PROMOTION_OPTIONS = new ArrayList<>(Arrays.asList(
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.ROOK,
            PieceType.QUEEN
    ));
}
