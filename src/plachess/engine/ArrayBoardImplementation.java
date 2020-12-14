package plachess.engine;

import java.util.*;

public class ArrayBoardImplementation {
    private final Map<PieceType, List<Position>> MOVE_DIRECTIONS;
    private final Map<PieceType, Integer> MOVE_RANGE;

    public ArrayBoardImplementation() {
        MOVE_DIRECTIONS = new HashMap<>();
        MOVE_RANGE = new HashMap<>();

        MOVE_DIRECTIONS.put(PieceType.KNIGHT, Arrays.asList(
                new Position(-2, -1),
                new Position(-2, +1),
                new Position(-1, -2),
                new Position(-1, +2),
                new Position(+1, -2),
                new Position(+1, +2),
                new Position(-1, +1),
                new Position(+2, -1),
                new Position(+2, +1)
        ));
        MOVE_DIRECTIONS.put(PieceType.ULTRA_KNIGHT, MOVE_DIRECTIONS.get(PieceType.KNIGHT));
        MOVE_DIRECTIONS.put(PieceType.BISHOP, Arrays.asList(
                new Position(-1, -1),
                new Position(-1, +1),
                new Position(+1, -1),
                new Position(+1, +1)
        ));
        MOVE_DIRECTIONS.put(PieceType.ROOK, Arrays.asList(
                new Position(-1, 0),
                new Position(0, -1),
                new Position(0, +1),
                new Position(+1, 0)
        ));
        MOVE_DIRECTIONS.put(PieceType.QUEEN, Arrays.asList(
                new Position(-1, -1),
                new Position(-1, 0),
                new Position(-1, +1),
                new Position(0, -1),
                new Position(0, +1),
                new Position(+1, -1),
                new Position(+1, 0),
                new Position(+1, +1)
        ));
        MOVE_DIRECTIONS.put(PieceType.KING, MOVE_DIRECTIONS.get(PieceType.QUEEN));
        MOVE_RANGE.put(PieceType.KNIGHT, 1);
        MOVE_RANGE.put(PieceType.ULTRA_KNIGHT, Rules.BOARD_SIZE);
        MOVE_RANGE.put(PieceType.BISHOP, Rules.BOARD_SIZE);
        MOVE_RANGE.put(PieceType.ROOK, Rules.BOARD_SIZE);
        MOVE_RANGE.put(PieceType.QUEEN, Rules.BOARD_SIZE);
        MOVE_RANGE.put(PieceType.KING, 1);
    }

    /**
     * @return all reachable positions on board as if it was empty
     */
    public ArrayList<Position> getUnobstructedMoves(Piece piece) {
        return getMoves(piece, new EmptyBoard(), 1);
    }

    /**
     * @param mask 1=travel 2=capture
     * @return all reachable positions on board with regard to other pieces
     */
    public ArrayList<Position> getMoves(Piece piece, Board board, int mask) {
        if(piece.type == PieceType.PAWN)
            return getMovesPawn(piece, board, mask);
        List<Position> move_directions = MOVE_DIRECTIONS.get(piece.type);
        int move_range = MOVE_RANGE.get(piece.type);
        boolean retTravel=(mask&1)!=0,
                retCapture=(mask&2)!=0;

        ArrayList<Position> result = new ArrayList<>();
        for(Position direction: move_directions) {
            Position pos = piece.pos.add(direction);
            for(int dist=1; dist <= move_range; ++dist, pos=pos.add(direction)) {
                if(!pos.isValid()) break;
                if(board.isOccupied(pos)) {
                    if(board.getPiece(pos).color != piece.color)
                        if(retCapture) result.add(pos);
                    break;
                }
                if(retTravel) result.add(pos);
            }
        }
        return result;
    }

    /** @param mask 1=travel 2=capture */
    private ArrayList<Position> getMovesPawn(Piece piece, Board board, int mask) {
        boolean retTravel=(mask&1)!=0,
                retCapture=(mask&2)!=0;
        ArrayList<Position> result = new ArrayList<>();
        int homeRow = Rules.getColorHomeRow(piece.color);
        Position direction = new Position(0, Rules.getColorDirection(piece.color));
        boolean hasMoved = Rules.isPawnMoved(piece.pos, piece.color);

        Position next = piece.pos.add(direction);
        if(retTravel) {
            if (!board.isOccupied(next))
                result.add(next);
            if (!hasMoved) {
                next = next.add(direction);
                if (!board.isOccupied(next))
                    result.add(next);
            }
        }
        if(retCapture) {
            for (int x = -1; x < 2; x += 2) {
                next = piece.pos.add(new Position(x, direction.y));
                if (board.isOccupied(next) && board.getPiece(next).color != piece.color)
                    result.add(next);
            }
        }
        return result;
    }
}
