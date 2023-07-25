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
        Coroutine co = thunk.toCoroutine();
        assertEquals(123, (int) co.run());
        assertEquals(456, (int) co.run(true));
        assertEquals(666, (int) co.run());
    }

    @Test
    public void test2() {
        Thunk<Integer> thunk = exec(() -> System.out.println("begin"))
            .then(pause(123, Boolean.class))
            .flatMap(r -> r ? pause(456) : pause(789))
            .then(pause(666));
        Coroutine co = thunk.toCoroutine();
        assertEquals(123, (int) co.run());
        assertEquals(789, (int) co.run(false));
        assertEquals(666, (int) co.run());
    }
}
