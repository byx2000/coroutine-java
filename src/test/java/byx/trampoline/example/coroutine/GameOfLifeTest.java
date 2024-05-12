package byx.trampoline.example.coroutine;

import byx.trampoline.core.Coroutine;
import byx.trampoline.core.Trampoline;
import org.junit.jupiter.api.Test;

import java.util.List;

import static byx.trampoline.core.Trampolines.empty;
import static byx.trampoline.core.Trampolines.pause;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class GameOfLifeTest {
    @Test
    public void testGameOfLife() {
        char[][] grid = new char[][]{
            {'.', '.', '.', '*', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '*', '.', '.', '.', '.'},
            {'.', '.', '*', '*', '*', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
        };

        grid = oneGeneration(grid);
        assertArrayEquals(new char[][]{
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '*', '.', '*', '.', '.', '.', '.'},
            {'.', '.', '.', '*', '*', '.', '.', '.', '.'},
            {'.', '.', '.', '*', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
        }, grid);

        grid = oneGeneration(grid);
        assertArrayEquals(new char[][]{
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '*', '.', '.', '.', '.'},
            {'.', '.', '*', '.', '*', '.', '.', '.', '.'},
            {'.', '.', '.', '*', '*', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
        }, grid);

        grid = oneGeneration(grid);
        assertArrayEquals(new char[][]{
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '*', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '*', '*', '.', '.', '.'},
            {'.', '.', '.', '*', '*', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
        }, grid);

        grid = oneGeneration(grid);
        assertArrayEquals(new char[][]{
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '*', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '*', '.', '.', '.'},
            {'.', '.', '.', '*', '*', '*', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
        }, grid);
    }

    public static char[][] oneGeneration(char[][] grid) {
        int row = grid.length;
        int col = grid[0].length;
        char[][] nextGrid = new char[row][col];

        Coroutine co = simulate(row, col).toCoroutine();
        Object ret = co.run();
        while (ret != null) {
            if (ret instanceof Query q) {
                ret = co.run(grid[(q.r() + row) % row][(q.c() + col) % col] == '*');
            } else {
                Transition t = (Transition) ret;
                nextGrid[(t.r() + row) % row][(t.c() + col) % col] = (t.alive() ? '*' : '.');
                ret = co.run();
            }
        }

        return nextGrid;
    }

    public static Trampoline<?> simulate(int row, int col) {
        Trampoline<?> thunk = empty();
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                thunk = thunk.then(stepCell(r, c));
            }
        }
        return thunk.pause();
    }

    public static Trampoline<?> stepCell(int r, int c) {
        return pause(new Query(r, c), Boolean.class)
            .flatMap(state -> countNeighbors(r, c)
                .map(cnt -> gameLogic(state, cnt)))
            .flatMap(nextState -> pause(new Transition(r, c, nextState)));
    }

    public static Trampoline<Integer> countNeighbors(int r, int c) {
        return pause(new Query(r, c + 1), Boolean.class)
            .flatMap(s1 -> pause(new Query(r, c - 1), Boolean.class)
                .flatMap(s2 -> pause(new Query(r + 1, c), Boolean.class)
                    .flatMap(s3 -> pause(new Query(r - 1, c), Boolean.class)
                        .flatMap(s4 -> pause(new Query(r + 1, c + 1), Boolean.class)
                            .flatMap(s5 -> pause(new Query(r + 1, c - 1), Boolean.class)
                                .flatMap(s6 -> pause(new Query(r - 1, c + 1), Boolean.class)
                                    .flatMap(s7 -> pause(new Query(r - 1, c - 1), Boolean.class)
                                        .map(s8 -> List.of(s1, s2, s3, s4, s5, s6, s7, s8)))))))))
            .map(states -> (int) states.stream().filter(s -> s).count());
    }

    public static boolean gameLogic(boolean alive, int neighborCnt) {
        if (alive) {
            if (neighborCnt < 2 || neighborCnt > 3) {
                return false;
            }
        } else if (neighborCnt == 3) {
            return true;
        }
        return alive;
    }
}

record Query(int r, int c) {}

record Transition(int r, int c, boolean alive) {}
