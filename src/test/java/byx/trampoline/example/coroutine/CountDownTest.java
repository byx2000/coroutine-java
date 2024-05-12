package byx.trampoline.example.coroutine;

import byx.trampoline.core.Coroutine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static byx.trampoline.core.Trampolines.loop;
import static byx.trampoline.core.Trampolines.pause;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountDownTest {
    @Test
    public void testCountDown() {
        Coroutine co = countDown(5);
        assertEquals(5, (int) co.run());
        assertEquals(4, (int) co.run());
        assertEquals(10, (int) co.run(10));
        assertEquals(9, (int) co.run());
        assertEquals(8, (int) co.run());
        assertEquals(7, (int) co.run());
        assertEquals(15, (int) co.run(15));
        assertEquals(14, (int) co.run());
        assertEquals(13, (int) co.run());
        assertEquals(12, (int) co.run());
    }

    private Coroutine countDown(int n) {
        AtomicInteger cnt = new AtomicInteger(n);
        return loop(
            () -> cnt.get() > 0,
            () -> pause(cnt.get(), Integer.class).then(reset -> {
                if (reset != null) {
                    cnt.set(reset);
                } else {
                    cnt.decrementAndGet();
                }
            })
        ).toCoroutine();
    }
}
