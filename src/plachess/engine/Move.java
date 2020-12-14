package plachess.engine;

import java.util.ArrayList;
import java.util.Arrays;

public interface Move {
    /**
     * @return BoardPosition after applying specified move
     * can be null if the move is invalid (not all checks are performed)
     * use MoveEnpassant if performing en passant!
     */
    ArrayBoardPosition apply(ArrayBoardPosition bp);

    /** set false to specified positions in castling (if side is null, set both sides) */
    static void loseCastling(boolean[] castling, Color color, PieceType side) {
        if(side != null)
            castling[ArrayBoardPosition.castlingArrayIndex(color, side)] = false;
        else {
            castling[ArrayBoardPosition.castlingArrayIndex(color, PieceType.KING)] =
            castling[ArrayBoardPosition.castlingArrayIndex(color, PieceType.QUEEN)] = false;
        }
    }

    static boolean[] updateCastling(boolean[] castling, Piece moved) {
        boolean[] result = castling.clone();

        if(moved.type == PieceType.KING) {
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

        public ArrayBoardPosition apply(ArrayBoardPosition bp) {
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

            boolean[] newCastling = Move.updateCastling(bp.getCastling(), pieceFrom);
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

            return new ArrayBoardPosition(
                    newBoard, newTurnColor,
                    newCastling, newEnpassant,
                    newHalfMoveClock, newFullMoveClock
            );
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
        public ArrayBoardPosition apply(ArrayBoardPosition bp) {
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
            Position posRook = new Position(Rules.BORDER_KING, homeRow);
            Position posKing = new Position(Rules.COL_KING, homeRow);
            int dirKing = (int)Math.signum(posRook.x - posKing.x);
            Board newBoard = oldBoard.set(Arrays.asList(
                        new Pair<>(posKing, Piece.empty()),
                        new Pair<>(posRook, Piece.empty()),
                        new Pair<>(posKing.add(dirKing*2, 0), oldBoard.getPiece(posKing)),
                        new Pair<>(posKing.add(dirKing, 0), oldBoard.getPiece(posRook))
            ));

            return new ArrayBoardPosition(
                    newBoard, newTurnColor,
                    newCastling, null,
                    newHalfMoveClock, newFullMoveClock
            );
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
        public ArrayBoardPosition apply(ArrayBoardPosition bp) {
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
            if(pieceFrom.type != PieceType.PAWN ||
                    posTo.y != Rules.getColorHomeRow(pieceFrom.color.opposite()))
                return null;

            Color newTurnColor = bp.getTurnColor().opposite();

            int clockChange = newTurnColor != Rules.FIRST_TURN ? 1 : 0;
            int newHalfMoveClock = 0;
            int newFullMoveClock = bp.getFullMoveClock() + clockChange;

            Board newBoard = oldBoard.set(Arrays.asList(
                    new Pair<>(posFrom, Piece.empty()),
                    new Pair<>(posTo, new Piece(null, pieceFrom.color, promotion))
            ));

            return new ArrayBoardPosition(
                    newBoard, newTurnColor,
                    bp.getCastling(), null,
                    newHalfMoveClock, newFullMoveClock
            );
        }
    }

    class MoveEnpassant implements Move {
        public final Position posFrom, posTo;

        public MoveEnpassant(Position posFrom, Position posTo) {
            this.posFrom = posFrom;
            this.posTo = posTo;
        }

        @Override
        public ArrayBoardPosition apply(ArrayBoardPosition bp) {
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

            // valid enpassant
            ArrayList<Position> enpositions = Rules.getEnpassantInvolvedPositions(bp.getEnpassant());
            if(!(pieceFrom.type == PieceType.PAWN && capturing && pieceTo.type == PieceType.PAWN &&
                    enpositions != null && enpositions.get(0).equals(posTo) &&
                    enpositions.stream().skip(1).anyMatch(p -> p.equals(pieceFrom.pos))))
                return null;

            Color newTurnColor = bp.getTurnColor().opposite();

            int clockChange = newTurnColor != Rules.FIRST_TURN ? 1 : 0;
            int newHalfMoveClock = 0;
            int newFullMoveClock = bp.getFullMoveClock() + clockChange;

            Board newBoard = oldBoard.set(Arrays.asList(
                    new Pair<>(posFrom, Piece.empty()),
                    new Pair<>(enpositions.get(0), Piece.empty()),
                    new Pair<>(posTo, pieceFrom)
            ));

            return new ArrayBoardPosition(
                    newBoard, newTurnColor,
                    bp.getCastling(), null,
                    newHalfMoveClock, newFullMoveClock
            );
        }
    }
}
