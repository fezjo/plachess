package plachess.engine;

import java.util.ArrayList;

/**
 * Abstract class implementing some of higher function methods with assumption of Board interface
 */
public abstract class ArrayBoard implements Board {
    @Override
    public ArrayList<Move.MoveSimple> getSimpleMoves(Position from) {
        if(!isOccupied(from))
            return null;
        ArrayList<Move.MoveSimple> result = new ArrayList<>();
        Piece piece = getPiece(from);
        for(Position to: ArrayBoardImplementation.getMoves(this, piece, 3))
            result.add(new Move.MoveSimple(from, to));
        return result;
    }

    @Override
    public ArrayList<Position> getThreatening(Piece piece) {
        ArrayList<Position> result = new ArrayList<>();
        for(PieceType type: PieceType.values())
            for(Position pos: ArrayBoardImplementation.getMoves(this, new Piece(piece.pos, piece.color, type), 2))
                if(getPiece(pos).type == type)
                    result.add(pos);
        return result;
    }

    /** ignores enpassant */
    @Override
    public boolean isThreatened(Piece piece) {
        for(PieceType type: PieceType.values())
            for(Position pos: ArrayBoardImplementation.getMoves(this, new Piece(piece.pos, piece.color, type), 2))
                if(getPiece(pos).type == type)
                    return true;
        return false;
    }
}
