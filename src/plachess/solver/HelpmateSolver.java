package plachess.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import plachess.engine.BoardPosition;
import plachess.engine.Color;

public class HelpmateSolver implements Solver {
    private final BoardPosition state;
    private final int n;

    private ArrayList<BoardPosition> solutions;
    private ArrayList<Integer> numsOfMoves;
    private Integer numOfSolutions;

    /** @return the minimum full move clock or -1 if unsolvable.*/
     private static int recurse(BoardPosition state,int max_depth){
        if (state.getTurnColor() == Color.BLACK && state.isCheckMate()){
            return state.getFullMoveClock();
        }
        if (max_depth == 0 || state.isDraw()){
            return -1;
        }
        int best = -1;
        for (BoardPosition nextState : state.getMoves()){
            int numOfMoves = recurse(nextState, max_depth-1);
            if (numOfMoves != -1) {
                if (best == -1 || numOfMoves < best){
                    best = numOfMoves;
                }
            }
        }
        return best;
    }

    public HelpmateSolver(BoardPosition state, int n) {
        this.state = state;
        this.n = n;
    }

    @Override
    public void solve(){
        solutions = new ArrayList<BoardPosition>();
        numsOfMoves = new ArrayList<Integer>();
        numOfSolutions = 0;
        for (BoardPosition nextState : state.getMoves()){
            int numOfMoves = recurse(nextState, 2*n-1);
            if (numOfMoves != -1){
                solutions.add(nextState);
                numsOfMoves.add(numOfMoves - state.getFullMoveClock());
                numOfSolutions += 1;
            }
        }
    }

    @Override
    public int getN(){
        return n;
    }

    @Override
    public int getNumOfSolutions(){
        return numOfSolutions;
    }

    @Override
    public List<BoardPosition> getSolutions(){
        return Collections.unmodifiableList(solutions);
    }

    @Override
    public List<Integer> getNumsOfMoves(){
        return Collections.unmodifiableList(numsOfMoves);
    }
}
