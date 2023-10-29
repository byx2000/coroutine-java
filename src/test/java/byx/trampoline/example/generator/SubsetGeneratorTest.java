package byx.trampoline.example.generator;

import byx.trampoline.core.Coroutine;
import byx.trampoline.core.Trampoline;
import byx.trampoline.exception.EndOfCoroutineException;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static byx.trampoline.core.Trampolines.exec;
import static byx.trampoline.core.Trampolines.pause;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubsetGeneratorTest {
    @Test
    public void testSubsetGenerator1() {
        Coroutine generator = subsetGenerator(new int[]{1, 2, 3});
        assertEquals(List.of(), generator.run());
        assertEquals(List.of(3), generator.run());
        assertEquals(List.of(2), generator.run());
        assertEquals(List.of(2, 3), generator.run());
        assertEquals(List.of(1), generator.run());
        assertEquals(List.of(1, 3), generator.run());
        assertEquals(List.of(1, 2), generator.run());
        assertEquals(List.of(1, 2, 3), generator.run());
        assertThrows(EndOfCoroutineException.class, generator::run);
    }

    @Test
    public void testSubsetGenerator2() {
        int[] nums = IntStream.rangeClosed(1, 50).toArray();
        Coroutine generator = subsetGenerator(nums);
        assertEquals(List.of(), generator.run());
        assertEquals(List.of(50), generator.run());
        assertEquals(List.of(49), generator.run());
        assertEquals(List.of(49, 50), generator.run());
        assertEquals(List.of(48), generator.run());
        assertEquals(List.of(48, 50), generator.run());
        assertEquals(List.of(48, 49), generator.run());
        assertEquals(List.of(48, 49, 50), generator.run());
        assertEquals(List.of(47), generator.run());
        assertEquals(List.of(47, 50), generator.run());
    }

    private Coroutine subsetGenerator(int[] nums) {
        return subset(nums, 0, new LinkedList<>()).toCoroutine();
    }

    private Trampoline<List<Integer>> subset(int[] nums, int index, LinkedList<Integer> set) {
        if (index == nums.length) {
            return pause(set);
        }

        return exec(() -> subset(nums, index + 1, set))
            .then(() -> set.add(nums[index]))
            .then(() -> subset(nums, index + 1, set))
            .then(() -> set.removeLast());
    }
}
