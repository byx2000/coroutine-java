package byx.trampoline.example.recursion;

import byx.trampoline.core.Trampoline;
import org.junit.jupiter.api.Test;

import static byx.trampoline.core.Trampolines.exec;
import static byx.trampoline.core.Trampolines.value;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SumTest {
    @Test
    public void testSum() {
        for (int i = 1; i <= 1000; i++) {
            assertEquals(sum1(i), sum2(i).run());
        }
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class, () -> sum1(100000));
        assertEquals(5000050000L, sum2(100000).run());
    }

    private long sum1(int n) {
        if (n == 1) {
            return 1;
        }
        return sum1(n - 1) + n;
    }

    private Trampoline<Long> sum2(int n) {
        if (n == 1) {
            return value(1L);
        }
        return exec(() -> sum2(n - 1)).map(r -> r + n);
    }
}
