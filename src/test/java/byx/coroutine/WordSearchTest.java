package byx.coroutine;

import byx.coroutine.core.Thunk;
import org.junit.jupiter.api.Test;

import static byx.coroutine.core.Thunk.exec;
import static byx.coroutine.core.Thunk.value;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordSearchTest {
    @Test
    public void testWordSearch() {
        char[][] board = new char[][]{
            {'A', 'B', 'C', 'E'},
            {'S', 'F', 'C', 'S'},
            {'A', 'D', 'E', 'E'}
        };

        assertTrue(exist(board, "ABCCED"));
        assertTrue(exist(board, "SEE"));
        assertFalse(exist(board, "ABCB"));
    }

    public boolean exist(char[][] board, String word) {
        boolean[][] flag = new boolean[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (dfs(board, word, flag, i, j, 0).run()) {
                    return true;
                }
            }
        }
        return false;
    }

    private Thunk<Boolean> dfs(char[][] board, String word, boolean[][] flag, int r, int c, int index) {
        if (index == word.length()) {
            return value(true);
        }

        if (r < 0 || r >= board.length || c < 0 || c >= board[r].length) {
            return value(false);
        }

        if (flag[r][c]) {
            return value(false);
        }

        if (board[r][c] != word.charAt(index)) {
            return value(false);
        }

        return exec(() -> flag[r][c] = true)
            .then(() -> dfs(board, word, flag, r + 1, c, index + 1))
            .flatMap(b -> b ? value(true) : dfs(board, word, flag, r - 1, c, index + 1))
            .flatMap(b -> b ? value(true) : dfs(board, word, flag, r, c + 1, index + 1))
            .flatMap(b -> b ? value(true) : dfs(board, word, flag, r, c - 1, index + 1))
            .map(res -> {
                flag[r][c] = false;
                return res;
            });
    }
}
