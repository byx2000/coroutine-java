package byx.test;

import org.junit.jupiter.api.Test;

import static byx.test.Thunk.*;
import static org.junit.jupiter.api.Assertions.*;

public class FactorialTest {
    @Test
    public void testFactorial() {
        for (int i = 0; i <= 100; i++) {
           assertEquals(factorial1(i), factorial2(i).run());
        }
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class, () -> factorial1(100000));
        assertDoesNotThrow(() -> factorial2(100000).run());
    }

    private long factorial1(long n) {
        if (n == 0) {
            return 1;
        }
        return factorial1(n - 1) * n;
    }

    private Thunk<Long> factorial2(long n) {
        if (n == 0) {
            return value(1L);
        }
        return exec(() -> factorial2(n - 1)).map(r -> r * n);
    }
}
