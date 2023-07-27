package byx.test;

import byx.test.core.Thunk;
import byx.test.exception.StackOverflowException;
import org.junit.jupiter.api.Test;

import static byx.test.core.Thunk.*;
import static org.junit.jupiter.api.Assertions.*;

public class CatalanNumberTest {
    @Test
    public void testCatalan() {
        for (int i = 0; i <= 10; i++) {
            long ans = catalan1(i);
            assertEquals(ans, catalan2(i).run());
            assertEquals(ans, catalan3(i).run());
        }
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class, () -> catalan1(100000));
        assertThrows(StackOverflowException.class, () -> catalan2(100000).run(100000));
    }

    private long catalan1(long n) {
        if (n == 0 || n == 1) {
            return 1;
        }

        long res = 0;
        for (int i = 0; i <= n - 1; i++) {
            res += catalan1(i) * catalan1(n - 1 - i);
        }

        return res;
    }

    private Thunk<Long> catalan2(long n) {
        if (n == 0 || n == 1) {
            return value(1L);
        }

        Thunk<Long> res = value(0L);
        for (int i = 0; i <= n - 1; i++) {
            int t = i;
            Thunk<Long> rec = exec(() -> catalan2(t))
                .flatMap(a -> exec(() -> catalan2(n - 1 - t))
                    .map(b -> a * b));
            res = res.flatMap(r -> rec.map(p -> r + p));
        }

        return res;
    }

    private Thunk<Long> catalan3(long n) {
        if (n == 0 || n == 1) {
            return value(1L);
        }

        int[] i = new int[]{0};
        long[] res = new long[]{0};
        return loop(
            () -> i[0] <= n - 1,
            () -> i[0]++,
            exec(() -> catalan3(i[0]))
                .flatMap(a -> exec(() -> catalan3(n - 1 - i[0]))
                    .map(b -> a * b))
                .then(p -> res[0] += p)
        ).then(() -> value(res[0]));
    }
}
