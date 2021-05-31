package plachess.engine;

import java.util.List;

public interface BoardPosition {
    void destroy();

    /** @return reference to board */
    Board getBoard();
    Color getTurnColor();
    /**
     * use with castlingArrayIndex or use canCastle instead
     * @return copy of castling array (WK, WQ, BK, BQ)
     */
    boolean[] getCastling();
    /** @return enpassant position if exists, otherwise null */
    Position getEnpassant();
    int getHalfMoveClock();
    int getFullMoveClock();

    Piece getPiece(Position pos);
    default Piece getPiece(int x, int y) { return getPiece(new Position(x, y)); }
    PieceType getPieceType(Position pos);
    default PieceType getPieceType(int x, int y) { return getPieceType(new Position(x, y)); }
    Color getPieceColor(Position pos);
    default Color getPieceColor(int x, int y)  { return getPieceColor(new Position(x, y)); }

    boolean canCastle(Color color, PieceType side);

    /**
     * @param side KING or QUEEN side
     * @return index to castling array corresponding to information provided
     */
    static int castlingArrayIndex(Color color, PieceType side) {
        if(!Rules.isValidCastlingSide(side)) throw new IllegalArgumentException("Invalid castling side");
        return (color == Color.WHITE ? 0 : 2) + (side == PieceType.KING ? 0 : 1);
    }

    /**
     * generates all simple moves, adds specials moves (castling, enpassant), filters out invalid moves
     * handles promotion and moves not preventing checkmate
     * returned BoardPositions can be in check or draw (make sure by calling isDraw)
     * @return all BoardPositions created by a valid move from this BoardPosition
     */
    List<BoardPosition> getNextPositions();

    /** same as getNextPositions but updates Stats class for debugging purposes */
    List<BoardPosition> getNextPositions(Perft.Stats stats);

    /**
     * either side has exactly one, kings do not threaten each other
     * @return whether rules about kings are met
     */
    boolean isKingValid();

    /**
     * K vs K, K vs K&knight, K vs K&bishop, K&bishop vs K&bishop on same color)
     * @return whether the position is declared dead and therefore draw
     */
    boolean isDeadPosition();

    /**
     * will check fifty-move rule
     * will not check for threefold repetition rule
     * @return whether the current position can be called draw by some color
     */
    boolean canCallDraw();

    /**
     * will check for not being in check and not having legal move, dead positions, seventy-five-move rule
     * will not check for fivefold repetition rule
     * @return whether the current position is draw
     */
    default boolean isDraw() { return BoardPosition.isDraw(this, getNextPositions(), getTurnColor()); }

    /** @return whether the color is in check */
    boolean isCheck(Color color);


    /**
     * can be slow, use wisely
     * @return whether the color currently on turn has won
     */
    default boolean isCheckMate() { return BoardPosition.isCheckMate(this, getNextPositions(), getTurnColor()); }

    /**
     * will check for not being in check and not having legal move, dead positions, seventy-five-move rule
     * will not check for fivefold repetition rule
     * @return whether the provided position is draw provided that next is list of all possible moves from it
     */
    static boolean isDraw(BoardPosition curr, List<BoardPosition> next, Color color) {
        return curr.getHalfMoveClock() > Rules.DRAW_HALFMOVES_NOCLAIM ||
                curr.isDeadPosition() ||
                (curr.getHalfMoveClock() == Rules.DRAW_HALFMOVES_NOCLAIM && !isCheckMate(curr, next, color)) ||
                (!curr.isCheck(color) && allMovesCheck(next, color));
    }

    /**
     * @param curr current position of board
     * @param next all valid moves from curr
     * @return whether the color has been checkmated provided that next is list of all possible moves from it
     */
    public static boolean isCheckMate(BoardPosition curr, List<BoardPosition> next, Color color) {
        return curr.isCheck(color) && allMovesCheck(next, color);
    }

    /**
     * @param next list of moves
     * @return whether exists move in which color will not be in check
     */
    public static boolean allMovesCheck(List<BoardPosition> next, Color color) {
        return next.stream().allMatch(x -> x.isCheck(color));
    }

    public static BoardPosition fromXFEN(String xfen, Board board){
        String[] field = xfen.split(" ");

        board = Board.fromXFEN(board, field[0]);

        Color turnColor = field[1].equals("w") ? Color.WHITE : Color.BLACK;
        boolean[] castling = {false, false, false, false};
        if(field[2].contains("K"))
            castling[0] = true;
        if(field[2].contains("Q"))
            castling[1] = true;
        if(field[2].contains("k"))
            castling[2] = true;
        if(field[2].contains("q"))
            castling[3] = true;

        Position enpassant = null;
        if (!field[3].equals("-")){
            enpassant = new Position(field[3].charAt(0) - 'a',field[3].charAt(1) - '1');
        }

        int halfmoveClock = Integer.parseInt(field[4]);
        int fullmoveClock = Integer.parseInt(field[5]);

        return new ArrayBoardPosition(
                board, turnColor, castling, enpassant, halfmoveClock, fullmoveClock);
    }
}
