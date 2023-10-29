package byx.trampoline.example.recursion;

import byx.trampoline.core.Trampoline;
import byx.trampoline.exception.StackOverflowException;
import org.junit.jupiter.api.Test;

import static byx.trampoline.core.Trampolines.exec;
import static byx.trampoline.core.Trampolines.value;
import static org.junit.jupiter.api.Assertions.*;

public class FibonacciTest {
    @Test
    public void testFibonacci() {
        for (int i = 1; i <= 30; i++) {
            assertEquals(fibonacci1(i), fibonacci2(i).run());
        }
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class, () -> fibonacci1(100000));
        assertThrows(StackOverflowException.class, () -> fibonacci2(100000).run(10000));
    }

    private long fibonacci1(long n) {
        if (n == 1 || n == 2) {
            return 1;
        }
        return fibonacci1(n - 1) + fibonacci1(n - 2);
    }

    private Trampoline<Long> fibonacci2(long n) {
        if (n == 1 || n == 2) {
            return value(1L);
        }
        return exec(() -> fibonacci2(n - 1))
            .flatMap(a -> exec(() -> fibonacci2(n - 2))
                .map(b -> a + b));
    }
}
