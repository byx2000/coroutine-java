package byx.trampoline.example.generator;

import byx.trampoline.core.Generator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static byx.trampoline.core.Trampolines.pause;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FibonacciGeneratorTest {
    @Test
    public void testFibonacciGenerator() {
        Generator<Integer> generator1 = fibonacciGenerator(1, 2);
        List<Integer> nums1 = generator1.stream().limit(10).toList();
        assertEquals(List.of(1, 2, 3, 5, 8, 13, 21, 34, 55, 89), nums1);

        Generator<Integer> generator2 = fibonacciGenerator(3, 7);
        List<Integer> nums2 = generator2.stream().limit(10).toList();
        assertEquals(List.of(3, 7, 10, 17, 27, 44, 71, 115, 186, 301), nums2);
    }

    private Generator<Integer> fibonacciGenerator(int a, int b) {
        AtomicInteger x = new AtomicInteger(a);
        AtomicInteger y = new AtomicInteger(b);

        return pause(a)
            .loop(
                () -> true,
                () -> pause(y.get()).then(() -> {
                    int t = x.get();
                    x.set(y.get());
                    y.set(t + y.get());
                })
            )
            .toGenerator();
    }
}
