package byx.coroutine;

import byx.coroutine.core.Thunk;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static byx.coroutine.core.Thunks.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SudokuTest {
    @Test
    public void testSudoku() {
        char[][] board1 = new char[][]{
            {'5', '3', '.', '.', '7', '.', '.', '.', '.'},
            {'6', '.', '.', '1', '9', '5', '.', '.', '.'},
            {'.', '9', '8', '.', '.', '.', '.', '6', '.'},
            {'8', '.', '.', '.', '6', '.', '.', '.', '3'},
            {'4', '.', '.', '8', '.', '3', '.', '.', '1'},
            {'7', '.', '.', '.', '2', '.', '.', '.', '6'},
            {'.', '6', '.', '.', '.', '.', '2', '8', '.'},
            {'.', '.', '.', '4', '1', '9', '.', '.', '5'},
            {'.', '.', '.', '.', '8', '.', '.', '7', '9'}
        };

        char[][] board2 = new char[][]{
            {'5', '3', '.', '.', '7', '.', '.', '.', '.'},
            {'6', '.', '.', '1', '9', '5', '.', '.', '.'},
            {'.', '9', '8', '.', '.', '.', '.', '6', '.'},
            {'8', '.', '.', '.', '6', '.', '.', '.', '3'},
            {'4', '.', '.', '8', '.', '3', '.', '.', '1'},
            {'7', '.', '.', '.', '2', '.', '.', '.', '6'},
            {'.', '6', '.', '.', '.', '.', '2', '8', '.'},
            {'.', '.', '.', '4', '1', '9', '.', '.', '5'},
            {'.', '.', '.', '.', '8', '.', '.', '7', '9'}
        };

        char[][] ans = new char[][]{
            {'5','3','4','6','7','8','9','1','2'},
            {'6','7','2','1','9','5','3','4','8'},
            {'1','9','8','3','4','2','5','6','7'},
            {'8','5','9','7','6','1','4','2','3'},
            {'4','2','6','8','5','3','7','9','1'},
            {'7','1','3','9','2','4','8','5','6'},
            {'9','6','1','5','3','7','2','8','4'},
            {'2','8','7','4','1','9','6','3','5'},
            {'3','4','5','2','8','6','1','7','9'}
        };

        dfs1(board1, 0);
        assertArrayEquals(ans, board1);

        dfs2(board2, 0).run();
        assertArrayEquals(ans, board2);
    }

    private boolean dfs1(char[][] board, int pos) {
        if (pos == 81) {
            return true;
        }

        int r = pos / 9;
        int c = pos % 9;

        if (board[r][c] != '.') {
            return dfs1(board, pos + 1);
        }

        boolean exit = false;
        for (int i = 1; i <= 9 && !exit; i++) {
            board[r][c] = (char) (i + '0');
            if (checkRow(board, r, c) && checkCol(board, r, c) && checkBox(board, r, c)) {
                if (dfs1(board, pos + 1)) {
                    exit = true;
                }
            }
        }

        if (exit) {
            return true;
        }

        board[r][c] = '.';
        return false;
    }

    private Thunk<Boolean> dfs2(char[][] board, int pos) {
        if (pos == 81) {
            return value(true);
        }

        int r = pos / 9;
        int c = pos % 9;

        if (board[r][c] != '.') {
            return exec(() -> dfs2(board, pos + 1));
        }


        int[] i = new int[]{1};
        boolean[] exit = new boolean[]{false};

        return loop(
            () -> i[0] <= 9 && !exit[0],
            () -> i[0]++,
            exec(() -> {
                board[r][c] = (char) (i[0] + '0');
                if (checkRow(board, r, c) && checkCol(board, r, c) && checkBox(board, r, c)) {
                    return exec(() -> dfs2(board, pos + 1))
                        .then(res -> {
                            if (res) {
                                exit[0] = true;
                            }
                        });
                }
                return empty();
            })
        ).then(() -> {
            if (exit[0]) {
                return value(true);
            }
            board[r][c] = '.';
            return value(false);
        });
    }

    // 检查当前行是否有重复
    private boolean checkRow(char[][] board, int r, int c) {
        for (int i = 0; i < 9; i++) {
            if (i != c && board[r][i] == board[r][c]) {
                return false;
            }
        }
        return true;
    }

    // 检查当前列是否有重复
    private boolean checkCol(char[][] board, int r, int c) {
        for (int i = 0; i < 9; i++) {
            if (i != r && board[i][c] == board[r][c]) {
                return false;
            }
        }
        return true;
    }

    // 检查当前宫是否有重复
    private boolean checkBox(char[][] board, int r, int c) {
        // 计算(r, c)所在宫的左上角位置
        int r0 = r / 3 * 3;
        int c0 = c / 3 * 3;

        Set<Character> set = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                char d = board[r0 + i][c0 + j];
                if (d != '.' && set.contains(d)) {
                    return false;
                }
                set.add(d);
            }
        }

        return true;
    }
}
