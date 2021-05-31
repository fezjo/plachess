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
    private static int recurse(BoardPosition state, int maxDepth){
        int fail = maxDepth + 1;
        if(maxDepth < 0)
            return fail;
        Color turnColor = state.getTurnColor();

        if(state.isCheckMate())
            return turnColor == Color.BLACK ? 0 : fail;
        if (maxDepth == 0 || state.isDraw())
            return fail;

        int searchDepth = maxDepth;
        for (BoardPosition nextState : state.getNextPositions()){
            int foundDepth = recurse(nextState, searchDepth - 1) + 1;
            searchDepth = Math.min(searchDepth, foundDepth - 1);
        }
        return searchDepth + 1;
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
        int searchDepth = 2*n-1;
        for (BoardPosition nextState : state.getNextPositions()){
            int numOfMoves = recurse(nextState, searchDepth);
            if (numOfMoves <= searchDepth){
                solutions.add(nextState);
                numsOfMoves.add((numOfMoves + 1) / 2);
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
