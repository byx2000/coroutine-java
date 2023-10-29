package byx.trampoline.example.generator;

import byx.trampoline.core.Coroutine;
import byx.trampoline.core.Trampoline;
import byx.trampoline.exception.EndOfCoroutineException;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static byx.trampoline.core.Trampolines.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PermutationGeneratorTest {
    @Test
    public void testPermutationGenerator1() {
        Coroutine generator = permutationGenerator(new int[]{1, 2, 3});
        assertEquals(List.of(1, 2, 3), generator.run());
        assertEquals(List.of(1, 3, 2), generator.run());
        assertEquals(List.of(2, 1, 3), generator.run());
        assertEquals(List.of(2, 3, 1), generator.run());
        assertEquals(List.of(3, 1, 2), generator.run());
        assertEquals(List.of(3, 2, 1), generator.run());
        assertThrows(EndOfCoroutineException.class, generator::run);
    }

    @Test
    public void testPermutationGenerator2() {
        int[] nums = IntStream.rangeClosed(1, 20).toArray();
        Coroutine generator = permutationGenerator(nums);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), generator.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 19), generator.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 18, 20), generator.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 18), generator.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 18, 19), generator.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 19, 18), generator.run());
    }

    private Coroutine permutationGenerator(int[] nums) {
        return permutation(0, nums, new boolean[nums.length], new LinkedList<>()).toCoroutine();
    }

    private Trampoline<Void> permutation(int index, int[] nums, boolean[] flag, LinkedList<Integer> p) {
        if (index == nums.length) {
            return pause(p);
        }

        return loop(0, nums.length, i -> {
            if (!flag[i]) {
                return exec(() -> flag[i] = true)
                    .then(() -> p.addLast(nums[i]))
                    .then(() -> permutation(index + 1, nums, flag, p))
                    .then(() -> p.removeLast())
                    .then(() -> flag[i] = false);
            }
            return empty();
        });
    }
}
