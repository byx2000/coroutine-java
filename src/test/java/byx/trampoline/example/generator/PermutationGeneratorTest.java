package byx.trampoline.example.generator;

import byx.trampoline.core.Generator;
import byx.trampoline.core.Trampoline;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static byx.trampoline.core.Trampolines.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PermutationGeneratorTest {
    @Test
    public void testPermutationGenerator1() {
        Generator<List<Integer>> generator = permutationGenerator(new int[]{1, 2, 3});
        assertEquals(List.of(
            List.of(1, 2, 3),
            List.of(1, 3, 2),
            List.of(2, 1, 3),
            List.of(2, 3, 1),
            List.of(3, 1, 2),
            List.of(3, 2, 1)
        ), generator.stream().toList());
    }

    @Test
    public void testPermutationGenerator2() {
        int[] nums = IntStream.rangeClosed(1, 20).toArray();
        Generator<List<Integer>> generator = permutationGenerator(nums);
        assertEquals(List.of(
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20),
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 19),
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 18, 20),
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 18),
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 18, 19),
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 19, 18)
        ), generator.stream().limit(6).toList());
    }

    private Generator<List<Integer>> permutationGenerator(int[] nums) {
        return permutation(0, nums, new boolean[nums.length], new LinkedList<>()).toGenerator();
    }

    private Trampoline<Void> permutation(int index, int[] nums, boolean[] flag, LinkedList<Integer> p) {
        if (index == nums.length) {
            return pause(new ArrayList<>(p));
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
