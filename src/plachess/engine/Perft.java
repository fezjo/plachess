package plachess.engine;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * correctness testing class
 * checks number of reachable positions after some number of moves against official values
 * source of positions and results: https://www.chessprogramming.org/Perft_Results
 */
public class Perft {
    public static class Stats {
        public int depth = 0;
        public int nodes = 0;
        public int captures = 0;
        public int enpassants = 0;
        public int castles = 0;
        public int promotions = 0;
        public int checks = 0;
        public int checkmates = 0;
        public int draws = 0;

        public Stats() {
        }

        @Override
        public String toString() {
            return String.format(
                    "{depth:%d, nodes:%d, captures:%d, enpassant:%d, castles:%d, promotions:%d, checks:%d, checkmates:%d, draws:%d}",
                    depth, nodes, captures, enpassants, castles, promotions, checks, checkmates, draws);
        }
    }

    public static void recurse(BoardPosition bp, int depth, Stats stats) {
        if(depth == 0) {
            stats.nodes += 1;
            stats.checks += bp.isCheck(bp.getTurnColor()) ? 1 : 0;
//            stats.checkmates += bp.isCheckMate() ? 1 : 0;
//            stats.draws += bp.isDraw() ? 1 : 0;
            return;
        }
        if(depth < 0 || bp.isCheckMate() || bp.isDraw())
            return;

        for (BoardPosition next : bp.getNextPositions(depth == 1 ? stats : null)) {
            recurse(next, depth - 1, stats);
            next.destroy();
        }
    }

    public static boolean test(Board emptyBoard) {
        ArrayList<Pair<String, ArrayList<Integer>>> tests = new ArrayList<>(Arrays.asList(
                new Pair<>(
                        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        new ArrayList<>(Arrays.asList(1, 20, 400, 8902, 197281, 4865609))
                ),
                new Pair<>(
                        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
                        new ArrayList<>(Arrays.asList(1, 48, 2039, 97862, 4085603))
                ),
                new Pair<>(
                        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
                        new ArrayList<>(Arrays.asList(1, 14, 191, 2812, 43238, 674624, 11030083))
                ),
                new Pair<>(
                        "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
                        new ArrayList<>(Arrays.asList(1, 6, 264, 9467, 422333, 15833292))
                ),
                new Pair<>(
                        "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1",
                        new ArrayList<>(Arrays.asList(1, 6, 264, 9467, 422333, 15833292))
                ),
                new Pair<>(
                        "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
                        new ArrayList<>(Arrays.asList(1, 44, 1486, 62379, 2103487))
                ),
                new Pair<>(
                        "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
                        new ArrayList<>(Arrays.asList(1, 46, 2079, 89890, 3894594))
                )
            ));

        boolean ok = true;
        for(Pair<String, ArrayList<Integer>> test: tests) {
            String xfen = test.frst;
            ArrayList<Integer> nodes = test.scnd;
            for(int depth=0; depth < nodes.size(); ++depth) {
                int wantedResult = nodes.get(depth);
                if(wantedResult > 1e7) continue; // TODO remove
                System.out.printf("Testing %s in depth %d - should be %d\n", xfen, depth, wantedResult);
                Stats stats = new Stats();
                BoardPosition bp = BoardPosition.fromXFEN(xfen, emptyBoard);
                recurse(bp, depth, stats);
//                System.out.printf("Wrong number of nodes in test\n%s\nin depth %d should be %d but was %d\nStats: %s\n",
//                        xfen, depth, wantedResult, stats.nodes, stats);
                if(stats.nodes != wantedResult) {
                    System.out.printf("Wrong number of nodes in test\n%s\nin depth %d should be %d but was %d\nStats: %s\n",
                            xfen, depth, wantedResult, stats.nodes, stats);
                    ok = false;
                    break;
                }
            }
//            if(!ok)
//                break;
        }

        return ok;
    }
}
