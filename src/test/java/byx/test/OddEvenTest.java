package byx.test;

import org.junit.jupiter.api.Test;

import static byx.test.Thunk.*;
import static org.junit.jupiter.api.Assertions.*;

public class OddEvenTest {
    @Test
    public void testOddEven() {
        for (int i = 0; i <= 100; i++) {
            assertEquals(isOdd1(i), isOdd2(i).run());
            assertEquals(isEven1(i), isEven2(i).run());
        }
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class, () -> isOdd1(100000));
        assertThrows(StackOverflowError.class, () -> isEven1(100000));
        assertDoesNotThrow(() -> isOdd2(100000).run());
        assertDoesNotThrow(() -> isEven2(100000).run());
    }

    private boolean isOdd1(int n) {
        if (n == 0) {
            return false;
        }
        return isEven1(n - 1);
    }

    private boolean isEven1(int n) {
        if (n == 0) {
            return true;
        }
        return isOdd1(n - 1);
    }

    private Thunk<Boolean> isOdd2(int n) {
        if (n == 0) {
            return value(false);
        }
        return exec(() -> isEven2(n - 1));
    }

    private Thunk<Boolean> isEven2(int n) {
        if (n == 0) {
            return value(true);
        }
        return exec(() -> isOdd2(n - 1));
    }
}
