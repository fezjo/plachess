package plachess.engine;

import java.util.*;
import java.util.stream.Stream;

public class ArrayBoardPosition {
    private final Board board;
    private final Color turnColor;
    private final boolean[] castling;
    private final Position enpassant;
    private final int halfMoveClock, fullMoveClock;

    private Map<PieceType, Map<Color, ArrayList<Piece>>> pieces;
    private Map<Color, ArrayList<Position>> checking;

    ArrayBoardPosition(
            Board board, Color turnColor,
            boolean[] castling, Position enpassant,
            int halfMoveClock, int fullMoveClock) {
        this.board = board;
        this.turnColor = turnColor;
        this.castling = castling;
        this.enpassant = enpassant;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveClock = fullMoveClock;
        initialize();
    }

    private void initialize() {
        pieces = new HashMap<>();
        for(PieceType pt: PieceType.values()) {
            pieces.put(pt, new HashMap<>());
            for (Color c : Color.values())
                pieces.get(pt).put(c, new ArrayList<>());
        }

        for(Piece piece: board.getAllPieces())
            pieces.get(piece.type).get(piece.color).add(piece);

        checking = new HashMap<>();
        if(isKingValid())   // we will not look for checking pieces if kings are not meeting chess rules
            for(Color color: Color.values())
                checking.put(color, board.getThreatening(pieces.get(PieceType.KING).get(color).get(0)));
    }

    public ArrayBoardPosition getAfterMove(Move move) {
        return move.apply(this);
    }

    /** @return reference to board */
    public Board getBoard() { return board; }

    public Color getTurnColor() { return turnColor; }

    /** @return copy of castling array (WK, WQ, BK, BQ) */
    public boolean[] getCastling() { return castling.clone(); }

    public Position getEnpassant() { return enpassant; }

    public int getHalfMoveClock() { return halfMoveClock; }

    public int getFullMoveClock() { return fullMoveClock; }

    /** @return list of positions which check king of provided color */
    public List<Position> getChecking(Color color) {
        return Collections.unmodifiableList(checking.get(color));
    }

    public boolean canCastle(Color color, PieceType side) {
        return castling[castlingArrayIndex(color, side)];
    }

    /**
     * @param side KING or QUEEN side
     * @return index to castling array corresponding to information provided
     */
    public static int castlingArrayIndex(Color color, PieceType side) {
        if(!Rules.isValidCastlingSide(side)) throw new IllegalArgumentException("Invalid castling side");
        return (color == Color.WHITE ? 0 : 2) + (side == PieceType.KING ? 0 : 1);
    }

    /** board has to be oriented white side down */
    public ArrayList<Move.MoveCastling> getCastlingMoves() {
        ArrayList<Move.MoveCastling> result = new ArrayList<>();
        if(isCheck(turnColor))
            return result;
        Piece king = pieces.get(PieceType.KING).get(turnColor).get(0);
        if(canCastle(turnColor, PieceType.KING) &&
                Stream.of(5, 6).noneMatch(x -> board.isOccupied(x, king.pos.y)) &&
                Stream.of(5, 6).allMatch(x -> board.getThreatening(king.setPosition(x, king.pos.y)).isEmpty()))
            result.add(new Move.MoveCastling(turnColor, PieceType.KING));
        if(canCastle(turnColor, PieceType.QUEEN) &&
                Stream.of(1, 2, 3).noneMatch(x -> board.isOccupied(x, king.pos.y)) &&
                Stream.of(2, 3).allMatch(x -> board.getThreatening(king.setPosition(x, king.pos.y)).isEmpty()))
            result.add(new Move.MoveCastling(turnColor, PieceType.QUEEN));
        return result;
    }

    /**
     * generates all simple moves, adds specials moves (castling, enpassant), filters out invalid moves
     * handles promotion and moves not preventing checkmate
     * returned BoardPositions can be in check or draw (make sure by calling isDraw)
     * @return all BoardPositions created by a valid move from this BoardPosition
     */
    public ArrayList<ArrayBoardPosition> getMoves() {
        ArrayList<Move> moves = new ArrayList<Move>(board.getAllSimpleMoves(getTurnColor()));
        for(int i=0; i<moves.size(); ++i) {
            if(!(moves.get(i) instanceof Move.MoveSimple))  // TODO validte that this works as expected
                continue;
            Move.MoveSimple move = (Move.MoveSimple)moves.get(i);
            if(Rules.isPromotion(board.getPiece(move.posFrom), move.posTo))
                for(PieceType t: PieceType.values())
                    moves.add(new Move.MovePawnPromotion(move.posFrom, move.posTo, t));
        }

        if(enpassant != null) {
            ArrayList<Position> involved = Rules.getEnpassantInvolvedPositions(enpassant);
            Piece attacked = board.getPiece(involved.get(0));
            if(!Piece.isEmpty(attacked)) {
                for(int i=1; i<involved.size(); ++i) {
                    Piece attacking = board.getPiece(involved.get(i));
                    if(!Piece.isEmpty(attacking) && attacking.color != attacked.color)
                        moves.add(new Move.MoveEnpassant(attacking.pos, enpassant));
                }
            }
        }

        moves.addAll(getCastlingMoves());

        ArrayList<ArrayBoardPosition> result = new ArrayList<>();
        for(Move move: moves) {
            ArrayBoardPosition newBP = move.apply(this);
            if(!newBP.isKingValid() || newBP.isCheck(turnColor))
                continue;
            result.add(newBP);
        }

        return result;
    }

    /**
     * either side has exactly one, kings do not threaten each other
     * @return whether rules about kings are met
     */
    public boolean isKingValid() {
        return Stream.of(Color.values()).allMatch(c -> pieces.get(PieceType.KING).get(c).size() == 1) &&
                checking.get(turnColor).stream().noneMatch(p -> board.getPiece(p).type == PieceType.KING);
    }

    /**
     * K vs K, K vs K&knight, K vs K&bishop, K&bishop vs K&bishop on same color)
     * @return whether the position is declared dead and therefore draw
     */
    public boolean isDeadPosition() {
        for(PieceType t: Arrays.asList(PieceType.PAWN, PieceType.ROOK, PieceType.QUEEN))
            for(Color c: Color.values())
                if(!pieces.get(t).get(c).isEmpty())
                    return false;
        int kB = pieces.get(PieceType.KNIGHT).get(Color.BLACK).size();
        int kW = pieces.get(PieceType.KNIGHT).get(Color.WHITE).size();
        int bB = pieces.get(PieceType.BISHOP).get(Color.BLACK).size();
        int bW = pieces.get(PieceType.BISHOP).get(Color.WHITE).size();
        int sum = kB + kW + bB + bW;
        if(sum > 2)
            return false;
        if(sum < 2)
            return true;
        // sum == 2
        if(!(bB == 1 && bW == 1))
            return false;
        Position pB = pieces.get(PieceType.BISHOP).get(Color.BLACK).get(0).pos;
        Position pW = pieces.get(PieceType.BISHOP).get(Color.WHITE).get(0).pos;
        return (pB.x + pB.y) % 2 == (pW.x + pW.y) % 2;
    }

    /**
     * will check fifty-move rule
     * will not check for threefold repetition rule
     * @return whether the current position can be called draw by some color
     */
    public boolean canCallDraw() {
        return getHalfMoveClock() >= Rules.DRAW_HALFMOVES_CLAIM;
    }

    /**
     * will check for not being in check and not having legal move, dead positions, seventy-five-move rule
     * will not check for fivefold repetition rule
     * @return whether the current position is draw
     */
    public static boolean isDraw(ArrayBoardPosition curr, List<ArrayBoardPosition> next, Color color) {
        return curr.getHalfMoveClock() > Rules.DRAW_HALFMOVES_NOCLAIM ||
                curr.isDeadPosition() ||
                (curr.getHalfMoveClock() == Rules.DRAW_HALFMOVES_NOCLAIM && !isCheckMate(curr, next, color)) ||
                (!curr.isCheck(color) && allMovesCheck(next, color));
    }

    /** @return whether the color is in check */
    public boolean isCheck(Color color) {
        return !checking.get(color).isEmpty();
    }

    /**
     * @param curr current position of board
     * @param next all valid moves from curr
     * @return whether the color has been checkmated
     */
    public static boolean isCheckMate(ArrayBoardPosition curr, List<ArrayBoardPosition> next, Color color) {
        return curr.isCheck(color) && allMovesCheck(next, color);
    }

    /**
     * @param next list of moves
     * @return whether exists move in which color will not be in check
     */
    public static boolean allMovesCheck(List<ArrayBoardPosition> next, Color color) {
        return next.stream().allMatch(x -> x.isCheck(color));
    }
}
