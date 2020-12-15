package plachess.engine;

import java.util.ArrayList;
import java.util.List;

public class ArrayBoard implements Board {
    private final static ArrayBoardImplementation implementation = new ArrayBoardImplementation();
    private Piece[][] grid;

    public ArrayBoard() {
        grid = new Piece[Rules.BOARD_SIZE][Rules.BOARD_SIZE];
    }

    public ArrayBoard(ArrayBoard board) {
        this();
        for(int y=0; y<grid.length; ++y)
            grid[y] = board.grid[y].clone();
    }

    public ArrayBoard(Piece[][] board) {
        this();
        for(int y=0; y<grid.length; ++y)
            grid[y] = board[y].clone();
    }

    @Override
    public ArrayBoard clone() {
        return new ArrayBoard(this);
    }

    @Override
    public boolean isOccupied(Position pos) {
        return !Piece.isEmpty(getPiece(pos));
    }

    @Override
    public Piece getPiece(Position pos) {
        return grid[pos.y][pos.x];
    }

    @Override
    public ArrayList<Piece> getAllPieces() {
        ArrayList<Piece> result = new ArrayList<>();
        for(int y=0; y<grid.length; ++y)
            for(int x=0; x<grid[y].length; ++x)
                if(isOccupied(x, y))
                    result.add(getPiece(x, y));
        return result;
    }

    /**
     * returns null if position is not occupied,
     * otherwise ArrayList of pairs (from, to) moves
     */
    @Override
    public ArrayList<Move.MoveSimple> getSimpleMoves(Position from) {
        if(!isOccupied(from))
            return null;
        ArrayList<Move.MoveSimple> result = new ArrayList<>();
        Piece piece = getPiece(from);
        for(Position to: implementation.getMoves(piece, this, 3))
            result.add(new Move.MoveSimple(from, to));
        return result;
    }

    @Override
    public ArrayList<Move.MoveSimple> getAllSimpleMoves(Color color) {
        ArrayList<Move.MoveSimple> result = new ArrayList<>();
        for(Piece piece: getAllPieces()) {
            if(piece.color != color)
                continue;
            result.addAll(getSimpleMoves(piece.pos));
        }
        return result;
    }

    @Override
    public ArrayList<Position> getThreatening(Piece piece) {
        ArrayList<Position> result = new ArrayList<>();
        for(PieceType type: PieceType.values())
            result.addAll(implementation.getMoves(
                    new Piece(piece.pos, piece.color, type),
                    this, 2));
        return result;
    }

    @Override
    public ArrayBoard set(List<Pair<Position, Piece>> work) {
        ArrayBoard result = new ArrayBoard(this);
        for(Pair<Position, Piece> p: work) {
            Position pos = p.frst;
            result.grid[pos.y][pos.x] = p.scnd.setPos(pos);
        }
        return result;
    }

    public static boolean test() {
        String xfen = "rnbqkbnr/ppp1pppp/3p4/8/8/8/PPPPPPPP/RNBQKBNR";
        ArrayBoard board = (ArrayBoard)Board.fromXFEN(new ArrayBoard(), xfen);
        System.out.println(board.getPiece(0, 0));
        System.out.println(xfen);
        System.out.println(board.toXFEN());
        System.out.println(board.toStringGrid());
        System.out.println(board.getAllPieces());
        System.out.println(board.getAllSimpleMoves(Color.WHITE));
        System.out.println(board.getAllSimpleMoves(Color.BLACK));
        return true;
    }
}
