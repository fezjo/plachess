package plachess.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparseBoard extends ArrayBoard {
    private Map<Position, Piece> pieces;

    public SparseBoard() {
        pieces = new HashMap<>();
    }

    public SparseBoard(SparseBoard board) {
        this();
        pieces = new HashMap<>(board.pieces);
    }

    @Override
    public SparseBoard clone() {
        return new SparseBoard(this);
    }

    @Override
    public boolean isOccupied(Position pos) {
        return !Piece.isEmpty(getPiece(pos));
    }

    @Override
    public Piece getPiece(Position pos) {
        return pieces.getOrDefault(pos, null);
    }

    @Override
    public List<Piece> getAllPieces() {
        return new ArrayList<>(pieces.values());
    }

    @Override
    public SparseBoard set(List<Pair<Position, Piece>> work) {
        SparseBoard result = new SparseBoard(this);
        for(Pair<Position, Piece> p: work) {
            Position pos = p.frst;
            if (Piece.isEmpty(p.scnd))
                result.pieces.remove(pos);
            else
                result.pieces.put(pos, p.scnd.setPos(pos));
        }
        return result;
    }
}
