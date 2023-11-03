package byx.trampoline.example.generator;

import byx.trampoline.core.Generator;
import byx.trampoline.core.Trampoline;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static byx.trampoline.core.Trampolines.exec;
import static byx.trampoline.core.Trampolines.pause;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubsetGeneratorTest {
    @Test
    public void testSubsetGenerator1() {
        Generator<List<Integer>> generator = subsetGenerator(new int[]{1, 2, 3});
        assertEquals(List.of(
            List.of(),
            List.of(3),
            List.of(2),
            List.of(2, 3),
            List.of(1),
            List.of(1, 3),
            List.of(1, 2),
            List.of(1, 2, 3)
        ), generator.stream().toList());
    }

    @Test
    public void testSubsetGenerator2() {
        int[] nums = IntStream.rangeClosed(1, 50).toArray();
        Generator<List<Integer>> generator = subsetGenerator(nums);
        assertEquals(List.of(
            List.of(),
            List.of(50),
            List.of(49),
            List.of(49, 50),
            List.of(48),
            List.of(48, 50),
            List.of(48, 49),
            List.of(48, 49, 50),
            List.of(47),
            List.of(47, 50)
        ), generator.stream().limit(10).toList());
    }

    private Generator<List<Integer>> subsetGenerator(int[] nums) {
        return subset(nums, 0, new LinkedList<>()).toGenerator();
    }

    private Trampoline<Void> subset(int[] nums, int index, LinkedList<Integer> set) {
        if (index == nums.length) {
            return pause(new ArrayList<>(set));
        }

        return exec(() -> subset(nums, index + 1, set))
            .then(() -> set.add(nums[index]))
            .then(() -> subset(nums, index + 1, set))
            .then(() -> set.removeLast());
    }
}
