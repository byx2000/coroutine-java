package byx.coroutine;

import byx.coroutine.core.Coroutine;
import byx.coroutine.core.Thunk;
import byx.coroutine.exception.StackOverflowException;
import org.junit.jupiter.api.Test;

import static byx.coroutine.core.Thunks.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void testFibonacciIterator() {
        Coroutine co = fibonacciIterator().toCoroutine();
        for (int i = 1; i <= 30; i++) {
            assertEquals(fibonacci1(i), (long) co.run());
        }
    }

    private long fibonacci1(long n) {
        if (n == 1 || n == 2) {
            return 1;
        }
        return fibonacci1(n - 1) + fibonacci1(n - 2);
    }

    private Thunk<Long> fibonacci2(long n) {
        if (n == 1 || n == 2) {
            return value(1L);
        }
        return exec(() -> fibonacci2(n - 1))
            .flatMap(a -> exec(() -> fibonacci2(n - 2))
                .map(b -> a + b));
    }

    private Thunk<Long> fibonacciIterator() {
        long[] var = new long[]{1, 1, 1};
        return pause(var[1])
            .then(loop(
                () -> true,
                exec(() -> pause(var[1]))
                    .then(() -> var[2] = var[0])
                    .then(() -> var[0] = var[1])
                    .then(() -> var[1] += var[2])
            ));
    }
}
