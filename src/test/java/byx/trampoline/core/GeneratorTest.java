package byx.trampoline.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static byx.trampoline.core.Trampolines.pause;
import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTest {
    @Test
    public void testNext() {
        Generator<Integer> generator = pause(1).pause(2).pause(3).pause(4).pause(5).toGenerator();

        assertEquals(1, generator.next());
        assertTrue(generator.hasNext());
        assertEquals(2, generator.next());
        assertTrue(generator.hasNext());
        assertEquals(3, generator.next());
        assertTrue(generator.hasNext());
        assertEquals(4, generator.next());
        assertTrue(generator.hasNext());
        assertTrue(generator.hasNext());
        assertEquals(5, generator.next());
        assertFalse(generator.hasNext());
    }

    @Test
    public void testForLoop() {
        Generator<Integer> generator = pause(1).pause(2).pause(3).pause(4).pause(5).toGenerator();
        List<Integer> nums = new ArrayList<>();
        for (int i : generator) {
            nums.add(i);
        }
        assertEquals(List.of(1, 2, 3, 4, 5), nums);
    }

    @Test
    public void testStream() {
        Generator<Integer> generator = pause(1).pause(2).pause(3).pause(4).pause(5).toGenerator();
        Stream<Integer> stream = generator.stream();
        List<Integer> nums = stream.toList();
        assertEquals(List.of(1, 2, 3, 4, 5), nums);
    }
}
