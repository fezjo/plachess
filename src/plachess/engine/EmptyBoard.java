package plachess.engine;

import java.util.ArrayList;
import java.util.List;

public class EmptyBoard implements Board {
    @Override
    public Board clone() { return this; }

    @Override
    public boolean isOccupied(Position pos) { return false; }

    @Override
    public Piece getPiece(Position pos) {
        return Piece.empty();
    }

    @Override
    public ArrayList<Piece> getAllPieces() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Move.MoveSimple> getSimpleMoves(Position pos) {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Move.MoveSimple> getAllSimpleMoves(Color color) {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Position> getThreatening(Piece piece) {
        return new ArrayList<>();
    }

    @Override
    public Board set(List<Pair<Position, Piece>> work) {
        return this;
    }
}
