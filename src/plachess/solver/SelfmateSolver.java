package plachess.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import plachess.engine.BoardPosition;
import plachess.engine.Color;

public class SelfmateSolver implements Solver {
    private final BoardPosition state;
    private final int n;

    private ArrayList<BoardPosition> solutions;
    private ArrayList<Integer> numsOfMoves;
    private Integer numOfSolutions;

    /** @return the minimum full move clock or -1 if unsolvable.*/
     private static int recurse(BoardPosition state,int max_depth){
        if (state.getTurnColor() == Color.WHITE && state.isCheckMate()){ // TODO what if BLACK did checkmate?
            return state.getFullMoveClock();
        }
        if (max_depth == 0 || state.isDraw() || state.canCallDraw()){
            return -1;
        }
        int best = -1;
        if (state.getTurnColor() == Color.WHITE){
            for (BoardPosition nextState : state.getNextPositions()){
                int numOfMoves = recurse(nextState, max_depth-1);
                if (numOfMoves != -1) {
                    if (best == -1 || numOfMoves < best){
                        best = numOfMoves;
                    }
                }
            }
        }else{
            for (BoardPosition nextState : state.getNextPositions()){
                int numOfMoves = recurse(nextState, max_depth-1);
                if (numOfMoves != -1) {
                    if (best == -1 || numOfMoves > best){
                        best = numOfMoves;
                    }
                }else{
                    return -1;
                }
            }
        }

        return best;
    }

    public SelfmateSolver(BoardPosition state, int n) {
        this.state = state;
        this.n = n;
    }

    @Override
    public void solve(){
        solutions = new ArrayList<BoardPosition>();
        numsOfMoves = new ArrayList<Integer>();
        numOfSolutions = 0;
        for (BoardPosition nextState : state.getNextPositions()){
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
