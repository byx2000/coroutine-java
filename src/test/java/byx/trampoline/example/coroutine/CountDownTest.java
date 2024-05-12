package byx.trampoline.example.coroutine;

import byx.trampoline.core.Coroutine;
import byx.trampoline.exception.EndOfCoroutineException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static byx.trampoline.core.Trampolines.loop;
import static byx.trampoline.core.Trampolines.pause;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CountDownTest {
    @Test
    public void testCountDown() {
        Coroutine co1 = countDown(5);
        assertEquals(5, (int) co1.run());
        assertEquals(4, (int) co1.run());
        assertEquals(10, (int) co1.run(10));
        assertEquals(9, (int) co1.run());
        assertEquals(8, (int) co1.run());
        assertEquals(7, (int) co1.run());
        assertEquals(15, (int) co1.run(15));
        assertEquals(14, (int) co1.run());
        assertEquals(13, (int) co1.run());
        assertEquals(12, (int) co1.run());

        Coroutine co2 = countDown(3);
        assertEquals(3, (int) co2.run());
        assertEquals(2, (int) co2.run());
        assertEquals(1, (int) co2.run());
        assertThrows(EndOfCoroutineException.class, co2::run);
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
