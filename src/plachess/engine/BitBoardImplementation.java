package plachess.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

public class BitBoardImplementation {
    private static final ArrayList<PieceType> supportedPieceTypes = new ArrayList<>(Arrays.asList(
            PieceType.PAWN, PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN, PieceType.KING
    ));
    private static final EnumMap<PieceType, BitBoardLayer[]> attackPatterns = new EnumMap<>(PieceType.class);
    private static final EnumMap<Color, BitBoardLayer[]> attackPatternsPawn = new EnumMap<>(Color.class);
    private static final EnumMap<Color, BitBoardLayer[]> movePatternsPawn = new EnumMap<>(Color.class);

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


    public static ArrayList<Position> getMoves(BitBoard board, Piece piece, int mask) {
        return null;
    }
}
