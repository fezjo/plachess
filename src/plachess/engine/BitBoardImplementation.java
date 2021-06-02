package plachess.engine;

import javax.sql.ConnectionPoolDataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BitBoardImplementation {
    public static final ArrayList<PieceType> supportedPieceTypes = new ArrayList<>(Arrays.asList(
            PieceType.PAWN, PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN, PieceType.KING
    ));
    private static final EnumMap<PieceType, BitBoardLayer[]> attackPatterns = new EnumMap<>(PieceType.class);
    private static final EnumMap<Color, BitBoardLayer[]> attackPatternsPawn = new EnumMap<>(Color.class);
    private static final EnumMap<Color, BitBoardLayer[]> movePatternsPawn = new EnumMap<>(Color.class);
    private static final EnumMap<PieceType, ArrayList<Integer>> attackAngles =
        new EnumMap<PieceType, ArrayList<Integer>>(Stream.of(
            new Pair<>(PieceType.BISHOP, new ArrayList<>(Arrays.asList(1, 3))),
            new Pair<>(PieceType.ROOK, new ArrayList<>(Arrays.asList(0, 2))),
            new Pair<>(PieceType.QUEEN, new ArrayList<>(Arrays.asList(0, 2, 3, 1)))).
            collect(Collectors.toMap(p -> p.frst, p -> p.scnd))
    );
    private static final Position[] angleDirection = {
            Position.getNew(1, 0),
            Position.getNew(1, 1),
            Position.getNew(0, 1),
            Position.getNew(-1, 1)
    };

    static {
        // building attack masks for all piece types except pawn
        for(PieceType pt: supportedPieceTypes) {
            if(pt == PieceType.PAWN) continue;
            BitBoardLayer[] bblArray = new BitBoardLayer[BitBoardLayer.BA];
            BitBoardLayer.BitBoardLayerBuilder bblb = new BitBoardLayer.BitBoardLayerBuilder();
            for (int y = 0; y < BitBoardLayer.BS; ++y) {
                for (int x = 0; x < BitBoardLayer.BS; ++x) {
                    bblb.clear();
                    Piece piece = new Piece(Position.getNew(x, y), Color.WHITE, pt);
                    for (Position pos : ArrayBoardImplementation.getUnobstructedMoves(piece))
                        bblb.setCell(pos.x, pos.y, 1);
                    bblArray[BitBoardLayer.posToIndex(x, y)] = bblb.build();
//                    System.out.printf("pt=%s y=%d x=%d\n%s\n", pt, y, x, bblb.build());
                }
            }
            attackPatterns.put(pt, bblArray);
        }

        for(Color color: Color.values()) {
            int dirY = Rules.getColorDirection(color);
            BitBoardLayer[] bblArrayMove = new BitBoardLayer[BitBoardLayer.BA];
            BitBoardLayer[] bblArrayAttack = new BitBoardLayer[BitBoardLayer.BA];
            BitBoardLayer.BitBoardLayerBuilder bblb = new BitBoardLayer.BitBoardLayerBuilder();
            for (int y = 0; y < BitBoardLayer.BS; ++y) {
                for (int x = 0; x < BitBoardLayer.BS; ++x) {
                    Position currPos = Position.getNew(x, y);

                    bblb.clear();
                    Position nexPos = currPos.add(0, dirY);
                    if(Rules.isPositionValid(nexPos))
                        bblb.setCell(nexPos.x, nexPos.y, 1);
                    if(!Rules.isPawnMoved(currPos, color))
                        bblb.setCell(x, y + dirY * 2, 1);
                    bblArrayMove[BitBoardLayer.posToIndex(x, y)] = bblb.build();
//                    System.out.printf("move\t col=%s y=%d x=%d\n%s\n", color, y, x, bblb.build());

                    bblb.clear();
                    nexPos = currPos.add(-1, dirY);
                    if(Rules.isPositionValid(nexPos))
                        bblb.setCell(nexPos.x, nexPos.y, 1);
                    nexPos = currPos.add(+1, dirY);
                    if(Rules.isPositionValid(nexPos))
                        bblb.setCell(nexPos.x, nexPos.y, 1);
                    bblArrayAttack[BitBoardLayer.posToIndex(x, y)] = bblb.build();
//                    System.out.printf("attack\t col=%s y=%d x=%d\n%s\n", color, y, x, bblb.build());
                }
            }
            attackPatternsPawn.put(color, bblArrayAttack);
            movePatternsPawn.put(color, bblArrayMove);
        }

    }

    public static ArrayList<Position> getMoves(BitBoard board, Piece piece, int retMask) {
        if (Piece.isEmpty(piece))
            return new ArrayList<>();
        if(piece.type == PieceType.PAWN)
            return getMovesPawn(board, piece, retMask);
        boolean retTravel = (retMask & 1) != 0,
                retCapture = (retMask & 2) != 0;
        BitBoardLayer attackPattern = attackPatterns.get(piece.type)[BitBoardLayer.posToIndex(piece.pos.x, piece.pos.y)];
        BitBoardLayer enemyLayer = board.colorLayers.get(piece.color.opposite());

        ArrayList<Position> result = new ArrayList<>();
        if(piece.type == PieceType.KNIGHT || piece.type == PieceType.KING) {
            if(retTravel) {
                BitBoardLayer freeLayer = board.occupiedLayer.not().and(attackPattern);
                result.addAll(freeLayer.getAllOnes());
            }
            if(retCapture) {
                BitBoardLayer attackLayer = enemyLayer.and(attackPattern);
                result.addAll(attackLayer.getAllOnes());
            }
        } else {
            BitBoardLayer blocking = board.occupiedLayer.and(attackPattern);
            for(int angle: attackAngles.get(piece.type)) {
                boolean isRot45 = (angle & 1) != 0;
                boolean isRot90 = (angle & 2) != 0;

                Position rotPos = isRot90 ?
                        Position.getNew(piece.pos.y, BitBoardLayer.BS-1 - piece.pos.x) :
                        Position.getNew(piece.pos.x, piece.pos.y);
                int rowPos = isRot45 ? Math.min(rotPos.x, rotPos.y) : rotPos.x;

                BitBoardLayer rotated = blocking.rotate(angle);

                byte row;
                if(isRot45)
                    row = BitBoardLayer.getDiagonalFromRotated45(rotated, rotPos.x, rotPos.y);
                else
                    row = rotated.getRow(rotPos.y);

                for(int dir = -1; dir <= 1; dir += 2) {
                    Position dirPos = Position.getNew(angleDirection[angle].x * dir, angleDirection[angle].y * dir);
                    Position pos = piece.pos;
                    for(int i = rowPos + dir; ; i += dir) {
                        pos = pos.add(dirPos);
                        if(!Rules.isPositionValid(pos))
                            break;
                        if((row & (1 << i)) == 0) {
                            if(retTravel)
                                result.add(pos);
                        } else {
                            if(retCapture && enemyLayer.isCell(pos.x, pos.y))
                                result.add(pos);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    public static ArrayList<Position> getMovesPawn(BitBoard board, Piece piece, int retMask) {
        boolean retTravel = (retMask & 1) != 0,
                retCapture = (retMask & 2) != 0;
        int dirY = Rules.getColorDirection(piece.color);
        int bi = BitBoardLayer.posToIndex(piece.pos.x, piece.pos.y);

        ArrayList<Position> result = new ArrayList<>();
        BitBoardLayer moveLayer;
        Position next;
        if(retTravel) {
            moveLayer = movePatternsPawn.get(piece.color)[bi].and(board.occupiedLayer.not());
            for (int y = 1; y <= 2; ++y) {
                next = piece.pos.add(0, y * dirY);
                if (Rules.isPositionValid(next) && moveLayer.isCell(next.x, next.y))
                    result.add(next);
                else
                    break;
            }
        }
        if(retCapture) {
            moveLayer = attackPatternsPawn.get(piece.color)[bi].and(board.colorLayers.get(piece.color.opposite()));
            for (int x = -1; x <= 1; x += 2) {
                next = piece.pos.add(x, dirY);
                if (Rules.isPositionValid(next) && moveLayer.isCell(next.x, next.y))
                    result.add(next);
            }
        }
        return result;
    }
}
