package plachess.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * board is always viewed from whites perspective
 * row and column numberings are from 0 to 7
 */
public interface Board {
    /** @return true if given position contains non-empty piece */
    default boolean isOccupied(Position pos) { return isOccupied(pos.x, pos.y); };
    /** @return true if given position contains non-empty piece */
    default boolean isOccupied(int x, int y) { return isOccupied(Position.getNew(x, y)); }

    /**
     * can throw exception if pos is not valid
     * @return Piece at position if occupied, otherwise null
     */
    default Piece getPiece(Position pos) { return getPiece(pos.x, pos.y); };
    default Piece getPiece(int x, int y) { return getPiece(Position.getNew(x, y)); }

    /** @return List of all non-empty pieces on board */
    List<Piece> getAllPieces();

    /**
     * @return all possible simple moves (travel/capture) by piece on given position
     *         or null if given position is empty
     */
    List<Move.MoveSimple> getSimpleMoves(Position pos);

    /** @return all possible simple moves (travel, capture) by all pieces of given color */
    default List<Move.MoveSimple> getAllSimpleMoves(Color color) {
        ArrayList<Move.MoveSimple> result = new ArrayList<>();
        for(Piece piece: getAllPieces()) {
            if(piece.color != color || !isOccupied(piece.pos))
                continue;
            result.addAll(getSimpleMoves(piece.pos));
        }
        return result;
    }

    /**
     * find all positions from which given piece is threatened by simple moves (ignores enpassant)
     * @return all pieces threatening given piece
     */
    List<Position> getThreatening(Piece piece);

    /**
     * (ignores enpassatn)
     * @return whether given piece is threatened by any piece of opposite color
     */
    boolean isThreatened(Piece piece);

    /**
     * create new board with some changes made (pieces moved/added/removed)
     * @param work List of positions to be replaced by given piece
     * @return new Board with all changes applied
     */
    Board set(List<Pair<Position, Piece>> work);


    static<B extends Board> Board fromXFEN(B emptyBoard, String xfen) {
        String fen_board = xfen.split(" ")[0];
        String[] rows = fen_board.split("/");
        Collections.reverse(Arrays.asList(rows));
        ArrayList<Pair<Position, Piece>> instructions = new ArrayList<>();
        for(int y=0; y<Rules.BOARD_SIZE; ++y) {
            int x=0;
            for(char c: rows[y].toCharArray()) {
                if(!Rules.char2piece.containsKey(c))
                    x += c - '0';
                else {
                    instructions.add(new Pair<>(Position.getNew(x, y), Rules.char2piece.get(c)));
                    ++x;
                }
            }
        }
        return emptyBoard.set(instructions);
    }

    /** @return board representation in XFEN (its first substring) */
    default String toXFEN() {
        StringBuilder sb = new StringBuilder();
        for(int y=Rules.BOARD_SIZE-1; y>=0; --y) {
            int empty=0;
            for(int x=0; x<Rules.BOARD_SIZE; ++x) {
                Piece p = this.getPiece(x, y);
                if(Piece.isEmpty(p))
                    empty++;
                else {
                    if(empty > 0) {
                        sb.append(empty);
                        empty = 0;
                    }
                    sb.append(Rules.piece2char.get(new Pair<>(p.color, p.type)));
                }
            }
            if(empty > 0) sb.append(empty);
            sb.append('/');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    /** @return 2D String representation of board */
    default String toStringGrid() {
        StringBuilder sb = new StringBuilder();
        for(int y=Rules.BOARD_SIZE-1; y>=0; --y) {
            for(int x=0; x<Rules.BOARD_SIZE; ++x) {
                Piece p = this.getPiece(x, y);
                if(Piece.isEmpty(p))
                    sb.append('.');
                else
                    sb.append(Rules.piece2char.get(new Pair<>(p.color, p.type)));
            }
            sb.append('\n');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}
