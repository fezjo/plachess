package plachess.engine;

import java.util.ArrayList;
import java.util.List;

public class Array2DBoard extends ArrayBoard {
    private final Piece[][] grid;

    public Array2DBoard() {
        grid = new Piece[Rules.BOARD_SIZE][Rules.BOARD_SIZE];
    }

    public Array2DBoard(Array2DBoard board) {
        this();
        for(int y=0; y<grid.length; ++y)
            grid[y] = board.grid[y].clone();
    }

    public Array2DBoard(Piece[][] board) {
        this();
        for(int y=0; y<grid.length; ++y)
            grid[y] = board[y].clone();
    }

    @Override
    public Array2DBoard clone() {
        return new Array2DBoard(this);
    }

    @Override
    public boolean isOccupied(int x, int y) {
        return !Piece.isEmpty(getPiece(x, y));
    }

    @Override
    public Piece getPiece(int x, int y) {
        return grid[y][x];
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

    @Override
    public Array2DBoard set(List<Pair<Position, Piece>> work) {
        Array2DBoard result = new Array2DBoard(this);
        for(Pair<Position, Piece> p: work) {
            Position pos = p.frst;
            if (p.scnd == null)
                result.grid[pos.y][pos.x] = null;
            else
                result.grid[pos.y][pos.x] = p.scnd.setPos(pos);
        }
        return result;
    }

    public static boolean test() {
        String xfen = "rnbqkbnr/ppp1pppp/3p4/8/8/8/PPPPPPPP/RNBQKBNR";
        ArrayBoard board = (ArrayBoard)Board.fromXFEN(new Array2DBoard(), xfen);
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
