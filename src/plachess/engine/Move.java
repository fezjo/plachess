package plachess.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface Move {
    /**
     * @return BoardPosition after applying specified move
     * can be null if the move is invalid (not all checks are performed)
     * use MoveEnpassant if performing en passant!
     */
    Pair<ArrayBoardPosition, Boolean> apply(ArrayBoardPosition bp);

    /** set false to specified positions in castling (if side is null, set both sides) */
    static void loseCastling(boolean[] castling, Color color, PieceType side) {
        if(side != null)
            castling[BoardPosition.castlingArrayIndex(color, side)] = false;
        else {
            castling[BoardPosition.castlingArrayIndex(color, PieceType.KING)] =
            castling[BoardPosition.castlingArrayIndex(color, PieceType.QUEEN)] = false;
        }
    }

    static boolean[] updateCastling(boolean[] castling, Piece moved) {
        boolean[] result = castling.clone();

        if(Piece.isEmpty(moved))
            return result;
        else if(moved.type == PieceType.KING) {
            loseCastling(result, moved.color, null);
        } else if(moved.type == PieceType.ROOK) {
            if(moved.color == Color.WHITE) {
                if (moved.pos.equals(Rules.BORDER_KING, Rules.ROW_WHITE))
                    loseCastling(result, moved.color, PieceType.KING);
                else if (moved.pos.equals(Rules.BORDER_QUEEN, Rules.ROW_WHITE))
                    loseCastling(result, moved.color, PieceType.QUEEN);
            } else { // if(moved.color == Color.BLACK)
                if (moved.pos.equals(Rules.BORDER_KING, Rules.ROW_BLACK))
                    loseCastling(result, moved.color, PieceType.KING);
                else if (moved.pos.equals(Rules.BORDER_QUEEN, Rules.ROW_BLACK))
                    loseCastling(result, moved.color, PieceType.QUEEN);
            }
        }
        return result;
    }

    static Position updateEnpassant(Piece piece, Position posTo) {
        int pawnDir = Rules.getColorDirection(piece.color);
        if(piece.type == PieceType.PAWN &&
                !Rules.isPawnMoved(piece.pos, piece.color) &&
                posTo.equals(piece.pos.add(0, 2 * pawnDir)))
            return piece.pos.add(0, pawnDir);
        return null;
    }

    static boolean isPromoted(Piece piece, Position posTo) {
        return piece.type == PieceType.PAWN &&
                posTo.y == Rules.getColorHomeRow(piece.color.opposite());
    }



    class MoveSimple implements Move {
        public final Position posFrom, posTo;

        public MoveSimple(Position posFrom, Position posTo) {
            this.posFrom = posFrom;
            this.posTo = posTo;
        }

        public Pair<ArrayBoardPosition, Boolean> apply(ArrayBoardPosition bp) {
            // valid positions
            if(!posFrom.isValid() || !posTo.isValid())
                return null;
            Board oldBoard = bp.getBoard();

            // valid capturing
            Piece pieceFrom = oldBoard.getPiece(posFrom);
            Piece pieceTo = oldBoard.getPiece(posTo);
            boolean capturing = !Piece.isEmpty(pieceTo);
            if(Piece.isEmpty(pieceFrom) || pieceFrom.color != bp.getTurnColor() ||
                    (capturing && pieceFrom.color == pieceTo.color))
                return null;

            boolean[] newCastling = Move.updateCastling(Move.updateCastling(bp.getCastling(), pieceFrom), pieceTo);
            Position newEnpassant = Move.updateEnpassant(pieceFrom, posTo);
            Color newTurnColor = bp.getTurnColor().opposite();

            int clockChange = newTurnColor != Rules.FIRST_TURN ? 1 : 0;
            int newHalfMoveClock = capturing || pieceFrom.type == PieceType.PAWN ?
                                    0 : bp.getHalfMoveClock() + clockChange;
            int newFullMoveClock = bp.getFullMoveClock() + clockChange;

            Board newBoard = oldBoard.set(Arrays.asList(
                    new Pair<>(posFrom, Piece.empty()),
                    new Pair<>(posTo, pieceFrom)
            ));

            return new Pair<>(
                new ArrayBoardPosition(
                    newBoard, newTurnColor,
                    newCastling, newEnpassant,
                    newHalfMoveClock, newFullMoveClock),
                capturing);
        }

        @Override
        public int hashCode() {
            return Objects.hash(posFrom, posTo);
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null || obj.getClass() != getClass()) return false;
            MoveSimple that = (MoveSimple) obj;
            return this.posFrom.equals(that.posFrom) && this.posTo.equals(that.posTo);
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return this;
        }

        @Override
        public String toString() {
            return String.format("MoveSimple(%s->%s)", posFrom, posTo);
        }
    }

    class MoveCastling implements Move {
        public final Color color;
        public final PieceType side;

        public MoveCastling(Color color, PieceType side) {
            this.color = color;
            this.side = side;
        }

        @Override
        public Pair<ArrayBoardPosition, Boolean> apply(ArrayBoardPosition bp) {
            // valid positions
            if(!Rules.isValidCastlingSide(side) || !bp.canCastle(color, side))
                return null;

            Board oldBoard = bp.getBoard();

            boolean[] newCastling = bp.getCastling();
            Move.loseCastling(newCastling, color, null);
            Color newTurnColor = bp.getTurnColor().opposite();

            int clockChange = newTurnColor != Rules.FIRST_TURN ? 1 : 0;
            int newHalfMoveClock = bp.getHalfMoveClock() + clockChange; // castling does not reset half move clock
            int newFullMoveClock = bp.getFullMoveClock() + clockChange;

            int homeRow = Rules.getColorHomeRow(color);
            Position posRook = new Position(this.side == PieceType.KING ? Rules.BORDER_KING : Rules.BORDER_QUEEN, homeRow);
            Position posKing = new Position(Rules.COL_KING, homeRow);
            int dirKing = (int)Math.signum(posRook.x - posKing.x);
            Board newBoard = oldBoard.set(Arrays.asList(
                        new Pair<>(posKing, Piece.empty()),
                        new Pair<>(posRook, Piece.empty()),
                        new Pair<>(posKing.add(dirKing*2, 0), oldBoard.getPiece(posKing)),
                        new Pair<>(posKing.add(dirKing, 0), oldBoard.getPiece(posRook))
            ));

            return new Pair<>(
                    new ArrayBoardPosition(
                            newBoard, newTurnColor,
                            newCastling, null,
                            newHalfMoveClock, newFullMoveClock),
                    false);
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, side);
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null || obj.getClass() != getClass()) return false;
            MoveCastling that = (MoveCastling) obj;
            return this.color == that.color && this.side == that.side;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return this;
        }

        @Override
        public String toString() {
            return String.format("MoveCastling(%s, %s)", color, side);
        }
    }

    class MovePawnPromotion implements Move {
        public final Position posFrom, posTo;
        public final PieceType promotion;

        public MovePawnPromotion(Position posFrom, Position posTo, PieceType promotion) {
            this.posFrom = posFrom;
            this.posTo = posTo;
            this.promotion = promotion;
        }

        @Override
        public Pair<ArrayBoardPosition, Boolean> apply(ArrayBoardPosition bp) {
            // valid positions
            if(!posFrom.isValid() || !posTo.isValid())
                return null;
            Board oldBoard = bp.getBoard();

            // valid capturing
            Piece pieceFrom = oldBoard.getPiece(posFrom);
            Piece pieceTo = oldBoard.getPiece(posTo);
            boolean capturing = !Piece.isEmpty(pieceTo);
            if(Piece.isEmpty(pieceFrom) || pieceFrom.color != bp.getTurnColor() ||
                    (capturing && pieceFrom.color == pieceTo.color))
                return null;

            // valid pawn promotion
            if(!Move.isPromoted(pieceFrom, posTo))
                return null;

            Color newTurnColor = bp.getTurnColor().opposite();

            int clockChange = newTurnColor != Rules.FIRST_TURN ? 1 : 0;
            int newHalfMoveClock = 0;
            int newFullMoveClock = bp.getFullMoveClock() + clockChange;

            Board newBoard = oldBoard.set(Arrays.asList(
                    new Pair<>(posFrom, Piece.empty()),
                    new Pair<>(posTo, new Piece(null, pieceFrom.color, promotion))
            ));

            return new Pair<>(
                    new ArrayBoardPosition(
                            newBoard, newTurnColor,
                            bp.getCastling(), null,
                            newHalfMoveClock, newFullMoveClock),
                    capturing);
        }

        @Override
        public int hashCode() {
            return Objects.hash(posFrom, posTo, promotion);
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null || obj.getClass() != getClass()) return false;
            MovePawnPromotion that = (MovePawnPromotion) obj;
            return this.posFrom.equals(that.posFrom) && this.posTo.equals(that.posTo) &&
                    this.promotion == that.promotion;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return this;
        }

        @Override
        public String toString() {
            return String.format("MovePawnPromotion(%s->%s ^%s)", posFrom, posTo, promotion);
        }
    }

    class MoveEnpassant implements Move {
        public final Position posFrom, posTo;

        public MoveEnpassant(Position posFrom, Position posTo) {
            this.posFrom = posFrom;
            this.posTo = posTo;
        }

        @Override
        public Pair<ArrayBoardPosition, Boolean> apply(ArrayBoardPosition bp) {
            // valid positions
            if(!posFrom.isValid() || !posTo.isValid())
                return null;
            Board oldBoard = bp.getBoard();
            Position posAttacked = new Position(posTo.x, posFrom.y);

            // valid capturing
            Piece pieceFrom = oldBoard.getPiece(posFrom);
            Piece pieceTo = oldBoard.getPiece(posTo);
            Piece pieceAttacked = oldBoard.getPiece(posAttacked);
            if(Piece.isEmpty(pieceFrom) || pieceFrom.color != bp.getTurnColor()
                    || Piece.isEmpty(pieceAttacked) || pieceFrom.color == pieceAttacked.color
                    || !Piece.isEmpty(pieceTo))
                return null;

            // valid enpassant
            ArrayList<Position> enpositions = Rules.getEnpassantInvolvedPositions(bp.getEnpassant());
            if(!(pieceFrom.type == PieceType.PAWN && pieceAttacked.type == PieceType.PAWN &&
                    enpositions != null &&
                    enpositions.get(0).equals(posAttacked) && enpositions.get(1).equals(posTo) &&
                    enpositions.stream().skip(2).anyMatch(p -> p.equals(pieceFrom.pos))))
                return null;

            Color newTurnColor = bp.getTurnColor().opposite();

            int clockChange = newTurnColor != Rules.FIRST_TURN ? 1 : 0;
            int newHalfMoveClock = 0;
            int newFullMoveClock = bp.getFullMoveClock() + clockChange;

            Board newBoard = oldBoard.set(Arrays.asList(
                    new Pair<>(posFrom, Piece.empty()),
                    new Pair<>(posAttacked, Piece.empty()),
                    new Pair<>(posTo, pieceFrom)
            ));

            return new Pair<>(
                    new ArrayBoardPosition(
                            newBoard, newTurnColor,
                            bp.getCastling(), null,
                            newHalfMoveClock, newFullMoveClock),
                    true);
        }

        @Override
        public int hashCode() {
            return Objects.hash(posFrom, posTo);
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null || obj.getClass() != getClass()) return false;
            MoveEnpassant that = (MoveEnpassant) obj;
            return this.posFrom.equals(that.posFrom) && this.posTo.equals(that.posTo);
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return this;
        }

        @Override
        public String toString() {
            return String.format("MoveEnpassant(%s->%s)", posFrom, posTo);
        }
    }
}
