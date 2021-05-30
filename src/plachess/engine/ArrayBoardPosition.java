package plachess.engine;

import java.util.*;
import java.util.stream.Stream;

public class ArrayBoardPosition implements BoardPosition {
    public static int spawnCount=0;

    private final Board board;
    private final Color turnColor;
    private final boolean[] castling;
    private final Position enpassant;
    private final int halfMoveClock, fullMoveClock;

    private Map<PieceType, Map<Color, ArrayList<Piece>>> pieces;
    private Map<Color, ArrayList<Position>> checking;
    private ArrayList<BoardPosition> nextMoves;

    public ArrayBoardPosition(
            Board board, Color turnColor,
            boolean[] castling, Position enpassant,
            int halfMoveClock, int fullMoveClock) {
        spawnCount += 1;
        this.board = board;
        this.turnColor = turnColor;
        this.castling = castling;
        this.enpassant = enpassant;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveClock = fullMoveClock;
        initialize();
    }

    private void initialize() {
        pieces = new EnumMap<>(PieceType.class);
        for(PieceType pt: PieceType.values()) {
            pieces.put(pt, new EnumMap<>(Color.class));
            for (Color c : Color.values())
                pieces.get(pt).put(c, new ArrayList<>());
        }

        for(Piece piece: board.getAllPieces())
            pieces.get(piece.type).get(piece.color).add(piece);

        checking = new EnumMap<>(Color.class);
        for(Color color: Color.values())
            checking.put(color, new ArrayList<Position>());
        if(isKingValid())   // we will not look for checking pieces if kings are not meeting chess rules
            for(Color color: Color.values())
                checking.put(color, board.getThreatening(pieces.get(PieceType.KING).get(color).get(0)));

        nextMoves = null;
    }

    public ArrayBoardPosition getAfterMove(Move move) {
        return move.apply(this);
    }

    @Override
    public Board getBoard() { return board; }

    @Override
    public Color getTurnColor() { return turnColor; }

    @Override
    public boolean[] getCastling() { return castling.clone(); }

    @Override
    public Position getEnpassant() { return enpassant; }

    @Override
    public int getHalfMoveClock() { return halfMoveClock; }

    @Override
    public int getFullMoveClock() { return fullMoveClock; }

    @Override
    public Piece getPiece(Position pos) {
        return board.getPiece(pos);
    }

    @Override
    public PieceType getPieceType(Position pos) {
        return board.getPiece(pos).type;
    }

    @Override
    public Color getPieceColor(Position pos) {
        return board.getPiece(pos).color;
    }

    /** @return list of positions which check king of provided color */
    public List<Position> getChecking(Color color) {
        return Collections.unmodifiableList(checking.get(color));
    }

    @Override
    public boolean canCastle(Color color, PieceType side) {
        return castling[BoardPosition.castlingArrayIndex(color, side)];
    }

    /** board has to be oriented white side down */
    public ArrayList<Move.MoveCastling> getCastlingMoves() {
        ArrayList<Move.MoveCastling> result = new ArrayList<>();
        if(isCheck(turnColor))
            return result;
        Piece king = pieces.get(PieceType.KING).get(turnColor).get(0);
        if(canCastle(turnColor, PieceType.KING) &&
                Stream.of(5, 6).noneMatch(x -> board.isOccupied(x, king.pos.y)) &&
                Stream.of(5, 6).allMatch(x -> board.getThreatening(king.setPos(x, king.pos.y)).isEmpty()))
            result.add(new Move.MoveCastling(turnColor, PieceType.KING));
        if(canCastle(turnColor, PieceType.QUEEN) &&
                Stream.of(1, 2, 3).noneMatch(x -> board.isOccupied(x, king.pos.y)) &&
                Stream.of(2, 3).allMatch(x -> board.getThreatening(king.setPos(x, king.pos.y)).isEmpty()))
            result.add(new Move.MoveCastling(turnColor, PieceType.QUEEN));
        return result;
    }

    @Override
    public List<BoardPosition> getMoves() {
        if(nextMoves != null)
            return nextMoves;

        ArrayList<Move> moves = new ArrayList<>(board.getAllSimpleMoves(getTurnColor()));
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

        ArrayList<BoardPosition> nextMoves = new ArrayList<>();
        for(Move move: moves) {
            ArrayBoardPosition newBP = move.apply(this);
            if(!newBP.isKingValid() || newBP.isCheck(turnColor))
                continue;
            nextMoves.add(newBP);
        }

        return nextMoves;
    }

    @Override
    public boolean isKingValid() {
        for(Color c: Color.values())
            if(pieces.get(PieceType.KING).get(c).size() != 1)
                return false;
        Map<Color, ArrayList<Piece>> kings = pieces.get(PieceType.KING);
        Position kingDistance = kings.get(Color.WHITE).get(0).pos.sub(kings.get(Color.BLACK).get(0).pos);
        if(Math.abs(kingDistance.x) + Math.abs(kingDistance.y) == 1)
            return false;
        return true;
//        return Stream.of(Color.values()).allMatch(c -> pieces.get(PieceType.KING).get(c).size() == 1) &&
//                checking.get(turnColor).stream().noneMatch(p -> board.getPiece(p).type == PieceType.KING);
    }

    @Override
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

    @Override
    public boolean canCallDraw() {
        return getHalfMoveClock() >= Rules.DRAW_HALFMOVES_CLAIM;
    }

    @Override
    public boolean isCheck(Color color) {
        return !checking.get(color).isEmpty();
    }

    public boolean test() {
        return true;
    }
}
