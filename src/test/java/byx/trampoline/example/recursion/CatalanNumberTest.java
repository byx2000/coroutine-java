package byx.trampoline.example.recursion;

import byx.trampoline.core.Trampoline;
import byx.trampoline.exception.StackOverflowException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static byx.trampoline.core.Trampolines.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private Trampoline<Long> catalan2(long n) {
        if (n == 0 || n == 1) {
            return value(1L);
        }

        Trampoline<Long> res = value(0L);
        for (int i = 0; i <= n - 1; i++) {
            int t = i;
            Trampoline<Long> rec = exec(() -> catalan2(t))
                .flatMap(a -> exec(() -> catalan2(n - 1 - t))
                    .map(b -> a * b));
            res = res.flatMap(r -> rec.map(p -> r + p));
        }

        return res;
    }

    private Trampoline<Long> catalan3(long n) {
        if (n == 0 || n == 1) {
            return value(1L);
        }

        AtomicLong res = new AtomicLong(0);
        return loop(0, (int) n, i ->
            exec(() -> catalan3(i))
                .flatMap(a -> exec(() -> catalan3(n - 1 - i))
                    .map(b -> a * b))
                .then(res::addAndGet)
        ).value(res::get);
    }
}
