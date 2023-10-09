package byx.coroutine;

import byx.coroutine.core.Thunk;
import org.junit.jupiter.api.Test;

import static byx.coroutine.core.Thunk.value;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThunkTest {
    @Test
    public void testValuePass1() {
        Thunk<Integer> thunk = value(123)
            .then(() -> System.out.println("hello"))
            .map(n -> n + 1);
        assertEquals(124, thunk.run());
    }

    @Test
    public void testValuePass2() {
        Thunk<Integer> thunk = value(123)
            .then(n -> assertEquals(123, n))
            .map(n -> n + 1);
        assertEquals(124, thunk.run());
    }
}
