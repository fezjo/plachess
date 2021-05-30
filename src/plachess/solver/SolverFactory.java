package plachess.solver;

import plachess.engine.BoardPosition;
import plachess.engine.ArrayBoardPosition;
import plachess.engine.Position;
import plachess.engine.Board;
import plachess.engine.ArrayBoard;
import plachess.engine.Array1DBoard;
import plachess.engine.SparseBoard;
import plachess.engine.Color;

import java.util.List;

public abstract class SolverFactory{
    public static Solver makeSolver(BoardPosition state, int n, String type){
        if (type.equals("selfmate"))
            return new SelfmateSolver(state, n);
        if (type.equals("helpmate"))
            return new HelpmateSolver(state, n);
        return null;
    }

    public static Solver makeSolverFromXFEN(String xfen){
        BoardPosition state = BoardPosition.fromXFEN(xfen, new Array1DBoard());

        String[] field = xfen.split(" ");
        String type = field[6];
        int n = Integer.parseInt(field[7]);

        return makeSolver(state, n, type);
    }
}
