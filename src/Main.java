import plachess.engine.ArrayBoard;
import plachess.engine.Board;
import plachess.engine.BoardPosition;
import plachess.engine.ArrayBoardPosition;
import plachess.engine.Color;
import plachess.solver.Solver;
import plachess.solver.SolverFactory;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Helpmates
        // String xfen = "8/8/N7/8/8/6q1/3K2k1/3b4 b - - 101 8";
        // String xfen = "1RrB2b1/8/4n3/2n3p1/2K2b2/1p1rk3/6BR/8 b - - 101 2";
        // String xfen = "8/8/b7/8/1R4K1/k2N4/8/8 b - - 101 2";

        // Selfmates
        // String xfen = "8/8/8/7p/4q2k/5Q2/5pKn/2B2BbR w - - 100 2";
        // String xfen = "8/8/Bn1R3Q/1R2N3/3N4/2k5/8/2Kb2rr w - - 100 2";

        Scanner scanner = new Scanner(System.in);
        String xfen = scanner.nextLine();

        Solver solver = SolverFactory.makeSolverFromXFEN(xfen);
        solver.solve();
        List<BoardPosition> solutions = solver.getSolutions();
        List<Integer> numsOfMoves = solver.getNumsOfMoves();
        for(int i=0; i<solver.getNumOfSolutions(); i++){
            if (numsOfMoves.get(i) < solver.getN())
                System.out.println(String.format(
                        "%s solved in %d moves",
                        solutions.get(i).getBoard().toXFEN(),
                        numsOfMoves.get(i)));
            else
                System.out.println(String.format(
                        "%s",
                        solutions.get(i).getBoard().toXFEN()));
        }
    }
}