package plachess.engine;

import java.util.ArrayList;
import java.util.List;

public class Array1DBoard extends ArrayBoard {
    private Piece[] grid;

    public Array1DBoard() {
        grid = new Piece[Rules.BOARD_SIZE * Rules.BOARD_SIZE];
    }

    public Array1DBoard(Array1DBoard board) {
        this();
        grid = board.grid.clone();
    }

    public Array1DBoard(Piece[] board) {
        this();
        grid = board.clone();
    }

    @Override
    public Array1DBoard clone() {
        return new Array1DBoard(this);
    }

    @Override
    public boolean isOccupied(int x, int y) {
        return !Piece.isEmpty(getPiece(x, y));
    }

    public boolean isOccupied(int i) {
        return !Piece.isEmpty(grid[i]);
    }

    @Override
    public Piece getPiece(int x, int y) {
        return grid[y * Rules.BOARD_SIZE + x];
    }

    @Override
    public List<Piece> getAllPieces() {
        ArrayList<Piece> result = new ArrayList<>();
        for(int i=0; i<grid.length; ++i)
            if(isOccupied(i))
                result.add(grid[i]);
        return result;
    }

    @Override
    public Array1DBoard set(List<Pair<Position, Piece>> work) {
        Array1DBoard result = new Array1DBoard(this);
        for(Pair<Position, Piece> p: work) {
            Position pos = p.frst;
            if (p.scnd == null)
                result.grid[pos.y*Rules.BOARD_SIZE + pos.x] = null;
            else
                result.grid[pos.y*Rules.BOARD_SIZE + pos.x] = p.scnd.setPos(pos);
        }
        return result;
    }
}
