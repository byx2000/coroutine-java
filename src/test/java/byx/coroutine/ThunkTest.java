package byx.coroutine;

import byx.coroutine.core.Coroutine;
import byx.coroutine.core.Thunk;
import org.junit.jupiter.api.Test;

import static byx.coroutine.core.Thunks.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThunkTest {
    @Test
    public void testLoopForever() {
        StringBuffer sb = new StringBuffer();
        Coroutine coroutine = exec(() -> sb.append("hello"))
            .pause()
            .loopForever()
            .toCoroutine();

        for (int i = 0; i < 100; i++) {
            coroutine.run();
        }

        assertEquals("hello".repeat(100), sb.toString());
    }

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
