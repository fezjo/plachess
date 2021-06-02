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

    private Map<Color, Piece> kings;
    private Map<Color, Boolean> inCheck;
    private Map<Color, Map<PieceType, Integer>> pieceCount;
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
        kings = new EnumMap<>(Color.class);
        pieceCount = new EnumMap<>(Color.class);
        for(Color c: Color.values()) {
            pieceCount.put(c, new EnumMap<>(PieceType.class));
            for (PieceType pt: PieceType.values())
                pieceCount.get(c).put(pt, 0);
        }

        for(Piece piece: board.getAllPieces()) {
            pieceCount.get(piece.color).compute(piece.type, (key, val) -> val + 1);
            if(piece.type == PieceType.KING)
                kings.put(piece.color, piece);
        }

        inCheck = new EnumMap<>(Color.class);
        if(isKingValid())   // we will not look for checking pieces if kings are not meeting chess rules
            for(Color color: Color.values())
                inCheck.put(color, board.isThreatened(kings.get(color)));

        nextMoves = null;
    }

    public void destroy() {
        nextMoves = null;
    }

    public ArrayBoardPosition getAfterMove(Move move) {
        return move.apply(this).frst;
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

    @Override
    public boolean canCastle(Color color, PieceType side) {
        return castling[BoardPosition.castlingArrayIndex(color, side)];
    }

    /** board has to be oriented white side down */
    public ArrayList<Move.MoveCastling> getCastlingMoves() {
        ArrayList<Move.MoveCastling> result = new ArrayList<>();
        if(isCheck(turnColor))
            return result;
        Piece king = kings.get(turnColor);
        if(canCastle(turnColor, PieceType.KING) &&
                Stream.of(5, 6).noneMatch(x -> board.isOccupied(x, king.pos.y)) &&
                Stream.of(5, 6).noneMatch(x -> board.isThreatened(king.setPos(x, king.pos.y))))
            result.add(new Move.MoveCastling(turnColor, PieceType.KING));
        if(canCastle(turnColor, PieceType.QUEEN) &&
                Stream.of(1, 2, 3).noneMatch(x -> board.isOccupied(x, king.pos.y)) &&
                Stream.of(2, 3).noneMatch(x -> board.isThreatened(king.setPos(x, king.pos.y))))
            result.add(new Move.MoveCastling(turnColor, PieceType.QUEEN));
        return result;
    }

    private ArrayList<Move> getMoves() {
        ArrayList<Move> moves = new ArrayList<>(board.getAllSimpleMoves(getTurnColor()));
        ArrayList<Move> newMoves = new ArrayList<>();
        for(Move move: moves) {
            if(!(move instanceof Move.MoveSimple)) { // TODO validate that this works as expected
                newMoves.add(move);
                continue;
            }
            Move.MoveSimple moveS = (Move.MoveSimple)move;
            if(Rules.isPromotion(board.getPiece(moveS.posFrom), moveS.posTo))
                for(PieceType t: Rules.PAWN_PROMOTION_OPTIONS)
                    newMoves.add(new Move.MovePawnPromotion(moveS.posFrom, moveS.posTo, t));
            else
                newMoves.add(move);
        }
        moves = newMoves;

        if(enpassant != null) {
            ArrayList<Position> involved = Rules.getEnpassantInvolvedPositions(enpassant);
            Piece attacked = board.getPiece(involved.get(0));
            if(!Piece.isEmpty(attacked)) {
                for(int i=2; i<involved.size(); ++i) {
                    Piece attacking = board.getPiece(involved.get(i));
                    if(!Piece.isEmpty(attacking) && attacking.type == PieceType.PAWN && attacking.color != attacked.color)
                        moves.add(new Move.MoveEnpassant(attacking.pos, enpassant));
                }
            }
        }

        moves.addAll(getCastlingMoves());

        return moves;
    }

    @Override
    public ArrayList<BoardPosition> getNextPositions() {
        if(nextMoves != null)
            return nextMoves;

        nextMoves = new ArrayList<>();
        for(Move move: getMoves()) {
            ArrayBoardPosition newBP = move.apply(this).frst;
            if(newBP == null || !newBP.isKingValid() || newBP.isCheck(turnColor))
                continue;
//            System.out.println(move);
            nextMoves.add(newBP);
        }

        return nextMoves;
    }

    public ArrayList<BoardPosition> getNextPositions(Perft.Stats stats) {
        if(stats == null) return getNextPositions();

        nextMoves = new ArrayList<>();
        for(Move move: getMoves()) {
            Pair<ArrayBoardPosition, Boolean> moveRes = move.apply(this);
            ArrayBoardPosition newBP = moveRes.frst;
            if(newBP == null || !newBP.isKingValid() || newBP.isCheck(turnColor))
                continue;

            if(moveRes.scnd) stats.captures++;
            if(move instanceof Move.MoveCastling) stats.castles++;
            if(move instanceof Move.MoveEnpassant) stats.enpassants++;
            if(move instanceof Move.MovePawnPromotion) stats.promotions++;

//            System.out.println(move);
            nextMoves.add(newBP);
        }

        return nextMoves;
    }

    @Override
    public boolean isKingValid() {
        for(Color c: Color.values())
            if(pieceCount.get(c).get(PieceType.KING) != 1)
                return false;
        Position kingDistance = kings.get(Color.WHITE).pos.sub(kings.get(Color.BLACK).pos);
        return Math.abs(kingDistance.x) + Math.abs(kingDistance.y) != 1;
    }

    @Override
    public boolean isDeadPosition() {
        for(PieceType t: Arrays.asList(PieceType.PAWN, PieceType.ROOK, PieceType.QUEEN))
            for(Color c: Color.values())
                if(pieceCount.get(c).get(t) > 0)
                    return false;

        int kB = pieceCount.get(Color.BLACK).get(PieceType.KNIGHT);
        int kW = pieceCount.get(Color.WHITE).get(PieceType.KNIGHT);
        int bB = pieceCount.get(Color.BLACK).get(PieceType.BISHOP);
        int bW = pieceCount.get(Color.WHITE).get(PieceType.BISHOP);
        int sum = kB + kW + bB + bW;
        if(sum > 2)
            return false;
        if(sum < 2)
            return true;
        // sum == 2
        if(!(bB == 1 && bW == 1))
            return false;

        Position pB=null, pW=null;
        for(Piece piece: board.getAllPieces()) // this should be hopefully ok as it is unlikely case
            if(piece.type == PieceType.BISHOP) {
                if (piece.color == Color.WHITE) pW = piece.pos;
                else pB = piece.pos;
            }
        return (pB.x + pB.y) % 2 == (pW.x + pW.y) % 2;
    }

    @Override
    public boolean canCallDraw() {
        return getHalfMoveClock() >= Rules.DRAW_HALFMOVES_CLAIM;
    }

    @Override
    public boolean isCheck(Color color) {
        return inCheck.get(color);
    }

    public boolean test() {
        return true;
    }
}
