import plachess.engine.*;
import plachess.solver.Solver;
import plachess.solver.SolverFactory;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if(false) {
            Perft.test();
            return;
        }
        // Helpmates
//         String xfen = "8/8/N7/8/8/6q1/3K2k1/3b4 b - - 0 1 helpmate 8";
//         String xfen = "1RrB2b1/8/4n3/2n3p1/2K2b2/1p1rk3/6BR/8 b - - 0 1 helpmate 2";
//         String xfen = "8/8/b7/8/1R4K1/k2N4/8/8 b - - 0 1 helpmate 2";
         String xfen = "RB6/6kq/8/8/3K4/8/6bb/8 b - - 0 1 helpmate 2";

        // Selfmates
//         String xfen = "8/8/8/7p/4q2k/5Q2/5pKn/2B2BbR w - - 0 1 selfmate 2";
//         String xfen = "8/8/Bn1R3Q/1R2N3/3N4/2k5/8/2Kb2rr w - - 0 1 selfmate 2";

//        Scanner scanner = new Scanner(System.in);
//        String xfen = scanner.nextLine();

        long start = System.currentTimeMillis();

        Solver solver = SolverFactory.makeSolverFromXFEN(xfen);
        solver.solve();
        List<BoardPosition> solutions = solver.getSolutions();
        List<Integer> numsOfMoves = solver.getNumsOfMoves();
        for(int i=0; i<solver.getNumOfSolutions(); i++){
            System.out.println(String.format(
                    "%s solved in %d moves",
                    solutions.get(i).getBoard().toXFEN(),
                    numsOfMoves.get(i)));
        }
        System.out.println(ArrayBoardPosition.spawnCount);


        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Time " + timeElapsed + "ms");
    }
}
