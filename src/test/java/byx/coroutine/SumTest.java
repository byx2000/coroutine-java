package byx.coroutine;

import byx.coroutine.core.Thunk;
import org.junit.jupiter.api.Test;

import static byx.coroutine.core.Thunks.exec;
import static byx.coroutine.core.Thunks.value;
import static org.junit.jupiter.api.Assertions.*;

public class SumTest {
    @Test
    public void testSum() {
        for (int i = 1; i <= 100; i++) {
            assertEquals(sum1(i), sum2(i).run());
        }
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class, () -> sum1(100000));
        assertDoesNotThrow(() -> sum2(100000).run());
    }

    private int sum1(int n) {
        if (n == 1) {
            return 1;
        }
        return sum1(n - 1) + n;
    }

    private Thunk<Integer> sum2(int n) {
        if (n == 1) {
            return value(1);
        }
        return exec(() -> sum2(n - 1)).map(r -> r + n);
    }
}
