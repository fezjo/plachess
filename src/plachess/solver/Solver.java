package plachess.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import plachess.engine.BoardPosition;

public interface Solver {
    public void solve();

    public int getN();

    public int getNumOfSolutions();

    public List<BoardPosition> getSolutions();

    public List<Integer> getNumsOfMoves();
}
