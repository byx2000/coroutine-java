package byx.test;

import org.junit.jupiter.api.Test;

import static byx.test.Thunk.*;
import static org.junit.jupiter.api.Assertions.*;

public class CoroutineTest {
    @Test
    public void test1() {
        Thunk<Integer> thunk = exec(() -> System.out.println("begin"))
            .then(pause(123, Boolean.class))
            .flatMap(r -> r ? pause(456) : pause(789))
            .then(pause(666));
        Coroutine<Integer> co = thunk.toCoroutine();
        assertEquals(123, co.run());
        assertEquals(456, co.run(true));
        assertEquals(666, co.run());
    }

    @Test
    public void test2() {
        Thunk<Integer> thunk = exec(() -> System.out.println("begin"))
            .then(pause(123, Boolean.class))
            .flatMap(r -> r ? pause(456) : pause(789))
            .then(pause(666));
        Coroutine<Integer> co = thunk.toCoroutine();
        assertEquals(123, co.run());
        assertEquals(789, co.run(false));
        assertEquals(666, co.run());
    }
}
