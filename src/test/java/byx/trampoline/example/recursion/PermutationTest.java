package byx.trampoline.example.recursion;

import byx.trampoline.core.Trampoline;
import byx.trampoline.exception.StackOverflowException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static byx.trampoline.core.Trampolines.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PermutationTest {
    @Test
    public void testPermutation() {
        for (int n = 1; n <= 9; n++) {
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = i + 1;
            }

            List<List<Integer>> ans = new ArrayList<>();
            permutation1(0, nums, new boolean[n], new LinkedList<>(), ans);
            List<List<Integer>> r1 = new ArrayList<>();
            permutation2(0, nums, new boolean[n], new LinkedList<>(), r1).run();
            assertEquals(ans, r1);
        }
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class,
            () -> permutation1(0, new int[10000], new boolean[10000], new LinkedList<>(), new ArrayList<>()));
        assertThrows(StackOverflowException.class,
            () -> permutation2(0, new int[10000], new boolean[10000], new LinkedList<>(), new ArrayList<>()).run(10000));
    }

    private void permutation1(int index, int[] nums, boolean[] flag, LinkedList<Integer> path, List<List<Integer>> result) {
        if (index == nums.length) {
            result.add(new ArrayList<>(path));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (!flag[i]) {
                flag[i] = true;
                path.addLast(nums[i]);
                permutation1(index + 1, nums, flag, path, result);
                path.removeLast();
                flag[i] = false;
            }
        }
    }

    private Trampoline<Void> permutation2(int index, int[] nums, boolean[] flag, LinkedList<Integer> path, List<List<Integer>> result) {
        if (index == nums.length) {
            result.add(new ArrayList<>(path));
            return empty();
        }

        return loop(0, nums.length, i -> {
            if (!flag[i]) {
                return exec(() -> flag[i] = true)
                    .then(() -> path.addLast(nums[i]))
                    .then(() -> permutation2(index + 1, nums, flag, path, result))
                    .then(() -> path.removeLast())
                    .then(() -> flag[i] = false);
            }
            return empty();
        });
    }
}
