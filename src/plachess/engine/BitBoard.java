package plachess.engine;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BitBoard implements Board { // TODO make variables final
    protected BitBoardLayer occupiedLayer;
    protected EnumMap<PieceType, BitBoardLayer> pieceLayers;
    protected EnumMap<Color, BitBoardLayer> colorLayers;

    public static class BitBoardBuilder extends BitBoard {

        public BitBoardBuilder() {
            this.occupiedLayer = new BitBoardLayer.BitBoardLayerBuilder();
            this.pieceLayers = new EnumMap<>(PieceType.class);
            this.colorLayers = new EnumMap<>(Color.class);
            for(PieceType pt: PieceType.values())
                this.pieceLayers.put(pt, new BitBoardLayer.BitBoardLayerBuilder());
            for(Color c: Color.values())
                this.colorLayers.put(c, new BitBoardLayer.BitBoardLayerBuilder());
        }

        public BitBoardBuilder(BitBoard bb) {
            this.occupiedLayer = new BitBoardLayer.BitBoardLayerBuilder(bb.occupiedLayer);
            this.pieceLayers = new EnumMap<>(PieceType.class);
            this.colorLayers = new EnumMap<>(Color.class);
            for(PieceType pt: PieceType.values())
                this.pieceLayers.put(pt, new BitBoardLayer.BitBoardLayerBuilder(bb.pieceLayers.get(pt)));
            for(Color c: Color.values())
                this.colorLayers.put(c, new BitBoardLayer.BitBoardLayerBuilder(bb.colorLayers.get(c)));
        }

        public BitBoard build() {
            BitBoard result = new BitBoard(null);
            result.occupiedLayer = BitBoardLayer.BitBoardLayerBuilder.build(result.occupiedLayer);
            for(PieceType pt: PieceType.values())
                result.pieceLayers.put(pt, new BitBoardLayer.BitBoardLayerBuilder(
                        BitBoardLayer.BitBoardLayerBuilder.build(this.pieceLayers.get(pt))));
            for(Color c: Color.values())
                result.colorLayers.put(c, new BitBoardLayer.BitBoardLayerBuilder(
                        BitBoardLayer.BitBoardLayerBuilder.build(this.colorLayers.get(c))));
            return result;
        }
    }

    public BitBoard() {
        this.occupiedLayer = new BitBoardLayer();
        this.pieceLayers = new EnumMap<>(PieceType.class);
        this.colorLayers = new EnumMap<>(Color.class);
        for(PieceType pt: PieceType.values())
            this.pieceLayers.put(pt, new BitBoardLayer());
        for(Color c: Color.values())
            this.colorLayers.put(c, new BitBoardLayer());
    }

    public BitBoard(BitBoard bb) {
        if(bb == null) {
            this.pieceLayers = new EnumMap<>(PieceType.class);
            this.colorLayers = new EnumMap<>(Color.class);
        } else {
            this.occupiedLayer = bb.occupiedLayer;
            this.pieceLayers = bb.pieceLayers.clone(); // TODO test that clone works
            this.colorLayers = bb.colorLayers.clone();
        }
    }

    @Override
    public boolean isOccupied(int x, int y) {
        return occupiedLayer.getCell(x, y) != 0;
    }

    @Override
    public Piece getPiece(int x, int y) {
        if(!isOccupied(x, y))
            return Piece.empty();
        for(PieceType pt: PieceType.values())
            if(pieceLayers.get(pt).getCell(x, y) != 0)
                return new Piece(
                        Position.getNew(x, y),
                        colorLayers.get(Color.WHITE).getCell(x, y) != 0 ? Color.WHITE : Color.BLACK,
                        pt);
        return Piece.empty();
    }

    @Override
    public ArrayList<Piece> getAllPieces() {
        ArrayList<Piece> result = new ArrayList<>();
        for(int y = 0; y < BitBoardLayer.BS; ++y) {
            for (int x = 0; x < BitBoardLayer.BS; ++x) {
                result.add(getPiece(x, y));
            }
        }
        return result;
    }

    @Override
    public ArrayList<Move.MoveSimple> getSimpleMoves(Position pos) {
        if(!isOccupied(pos))
            return null;
        ArrayList<Move.MoveSimple> result = new ArrayList<>();
        Piece piece = getPiece(pos);
        for(Position nextPos: _getSimpleMoves(piece, 3))
            result.add(new Move.MoveSimple(pos, nextPos));
        return result;
    }

    @Override
    public ArrayList<Position> getThreatening(Piece piece) {
        ArrayList<Position> result = new ArrayList<>();
        for(PieceType type: PieceType.values()) {
            BitBoardLayer layer = pieceLayers.get(type);
            if(layer == null) continue;
            for(Position pos: _getSimpleMoves(new Piece(piece.pos, piece.color, type), 2))
                if(layer.getCell(pos.x, pos.y) != 0)
                    result.add(pos);
        }
        return result;
    }

    @Override
    public boolean isThreatened(Piece piece) {
        for(PieceType type: PieceType.values()) {
            BitBoardLayer layer = pieceLayers.get(type);
            if(layer == null) continue;
            for(Position pos: _getSimpleMoves(new Piece(piece.pos, piece.color, type), 2))
                if(layer.getCell(pos.x, pos.y) != 0)
                    return true;
        }
        return false;
    }

    @Override
    public BitBoard set(List<Pair<Position, Piece>> work) {
        BitBoardBuilder bbb = new BitBoardBuilder(this);
        for(Pair<Position, Piece> p: work) {
            Position pos = p.frst;
            Piece newPiece = p.scnd;

            Piece oldPiece = bbb.getPiece(pos.x, pos.y);
            if(!Piece.isEmpty(oldPiece)) {
                bbb.colorLayers.get(oldPiece.color).setCell(pos.x, pos.y, 0);
                bbb.pieceLayers.get(oldPiece.type).setCell(pos.x, pos.y, 0);
            }

            if(Piece.isEmpty(newPiece)) {
                bbb.occupiedLayer.setCell(pos.x, pos.y, 0);
            } else {
                bbb.occupiedLayer.setCell(pos.x, pos.y, 1);
                bbb.colorLayers.get(newPiece.color).setCell(pos.x, pos.y, 1);
                bbb.pieceLayers.get(newPiece.type).setCell(pos.x, pos.y, 1);
            }

        }
        return bbb.build();
    }

    private ArrayList<Position> _getSimpleMoves(Piece piece, int mask) {
        return null;
    }
}
