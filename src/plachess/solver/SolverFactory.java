package plachess.solver;

import plachess.engine.BoardPosition;
import plachess.engine.ArrayBoardPosition;
import plachess.engine.Position;
import plachess.engine.ArrayBoard;
import plachess.engine.Board;
import plachess.engine.Color;

import java.util.List;

public abstract class SolverFactory{
    public static Solver makeSolver(BoardPosition state, int n, int type){
        if (type == 100)
            return new SelfmateSolver(state, n);
        if (type == 101)
            return new HelpmateSolver(state, n);
        return null;
    }

    public static Solver makeSolverFromXFEN(String xfen){
        String[] field = xfen.split(" ");

        ArrayBoard board = (ArrayBoard)Board.fromXFEN(new ArrayBoard(), field[0]);
        // We ignore field[1], in helpmate black moves always first and in
        // selfmate white moves always first.
        boolean[] castling = {false, false, false, false};
        if(field[2].contains("K"))
            castling[0] = true;
        if(field[2].contains("Q"))
            castling[1] = true;
        if(field[2].contains("k"))
            castling[2] = true;
        if(field[2].contains("q"))
            castling[3] = true;
        Position enpassant = null;
        if (!field[3].equals("-")){
            enpassant = new Position(field[3].charAt(0) - 97,field[3].charAt(1) - 49);
        }
        int type = Integer.parseInt(field[4]);
        Color turnColor = type == 100 ? Color.WHITE : Color.BLACK;
        int n = Integer.parseInt(field[5]);

        BoardPosition state = new ArrayBoardPosition(
                board, turnColor, castling, enpassant, 0, 1);
        return makeSolver(state, n, type);
    }
}
