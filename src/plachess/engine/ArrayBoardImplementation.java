package plachess.engine;

import java.util.*;

public class ArrayBoardImplementation {
    private static final Map<PieceType, List<Position>> MOVE_DIRECTIONS;
    private static final Map<PieceType, Integer> MOVE_RANGE;

    static {
        MOVE_DIRECTIONS = new EnumMap<>(PieceType.class);
        MOVE_RANGE = new EnumMap<>(PieceType.class);

        MOVE_DIRECTIONS.put(PieceType.KNIGHT, Arrays.asList(
                Position.getNew(-2, -1),
                Position.getNew(-2, +1),
                Position.getNew(-1, -2),
                Position.getNew(-1, +2),
                Position.getNew(+1, -2),
                Position.getNew(+1, +2),
                Position.getNew(+2, -1),
                Position.getNew(+2, +1)
        ));
        MOVE_DIRECTIONS.put(PieceType.ULTRA_KNIGHT, MOVE_DIRECTIONS.get(PieceType.KNIGHT));
        MOVE_DIRECTIONS.put(PieceType.BISHOP, Arrays.asList(
                Position.getNew(-1, -1),
                Position.getNew(-1, +1),
                Position.getNew(+1, -1),
                Position.getNew(+1, +1)
        ));
        MOVE_DIRECTIONS.put(PieceType.ROOK, Arrays.asList(
                Position.getNew(-1, 0),
                Position.getNew(0, -1),
                Position.getNew(0, +1),
                Position.getNew(+1, 0)
        ));
        MOVE_DIRECTIONS.put(PieceType.QUEEN, Arrays.asList(
                Position.getNew(-1, -1),
                Position.getNew(-1, 0),
                Position.getNew(-1, +1),
                Position.getNew(0, -1),
                Position.getNew(0, +1),
                Position.getNew(+1, -1),
                Position.getNew(+1, 0),
                Position.getNew(+1, +1)
        ));
        MOVE_DIRECTIONS.put(PieceType.KING, MOVE_DIRECTIONS.get(PieceType.QUEEN));
        MOVE_RANGE.put(PieceType.KNIGHT, 1);
        MOVE_RANGE.put(PieceType.ULTRA_KNIGHT, Rules.BOARD_SIZE);
        MOVE_RANGE.put(PieceType.BISHOP, Rules.BOARD_SIZE);
        MOVE_RANGE.put(PieceType.ROOK, Rules.BOARD_SIZE);
        MOVE_RANGE.put(PieceType.QUEEN, Rules.BOARD_SIZE);
        MOVE_RANGE.put(PieceType.KING, 1);
    }

    public ArrayBoardImplementation() {}

    /**
     * @return all reachable positions on board as if it was empty
     */
    public static ArrayList<Position> getUnobstructedMoves(Piece piece) {
        return getMoves(new EmptyBoard(), piece, 1);
    }

    /**
     * @param retMask 1=travel 2=capture
     * @return all reachable positions on board with regard to other pieces
     */
    public static ArrayList<Position> getMoves(Board board, Piece piece, int retMask) {
        if (Piece.isEmpty(piece))
            return new ArrayList<Position>();
        if(piece.type == PieceType.PAWN)
            return getMovesPawn(board, piece, retMask);
        List<Position> move_directions = MOVE_DIRECTIONS.get(piece.type);
        int move_range = MOVE_RANGE.get(piece.type);
        boolean retTravel = (retMask & 1) != 0,
                retCapture = (retMask & 2) != 0;

        ArrayList<Position> result = new ArrayList<>();
        for(Position direction: move_directions) {
            Position pos = piece.pos.add(direction);
            for(int dist=1; dist <= move_range; ++dist, pos=pos.add(direction)) {
                if(!Rules.isPositionValid(pos)) break;
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

    /**
     * @param retMask 1=travel 2=capture  */
    public static ArrayList<Position> getMovesPawn(Board board, Piece piece, int retMask) {
        boolean retTravel = (retMask & 1) != 0,
                retCapture = (retMask & 2) != 0;
        int dirY = Rules.getColorDirection(piece.color);
        boolean hasMoved = Rules.isPawnMoved(piece.pos, piece.color);

        ArrayList<Position> result = new ArrayList<>();
        Position next;
        if(retTravel) {
            next = piece.pos.add(0, dirY);
            boolean blocked = board.isOccupied(next);
            if (Rules.isPositionValid(next) && !blocked)
                result.add(next);
            if (!hasMoved && !blocked) {
                next = next.add(0, dirY);
                if (Rules.isPositionValid(next) && !board.isOccupied(next))
                    result.add(next);
            }
        }
        if(retCapture) {
            for (int x = -1; x <= 1; x += 2) {
                next = piece.pos.add(x, dirY);
                if (Rules.isPositionValid(next) && board.isOccupied(next) && board.getPiece(next).color != piece.color)
                    result.add(next);
            }
        }
        return result;
    }
}
