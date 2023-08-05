package byx.test;

import byx.test.core.Coroutine;
import byx.test.core.Thunk;
import byx.test.exception.EndOfCoroutineException;
import byx.test.exception.StackOverflowException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static byx.test.core.Thunk.*;
import static org.junit.jupiter.api.Assertions.*;

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
            List<List<Integer>> r2 = new ArrayList<>();
            permutation3(0, nums, new boolean[n], new LinkedList<>(), r2).run();
            List<List<Integer>> r3 = new ArrayList<>();
            permutation4(0, nums, new boolean[n], new LinkedList<>(), r3).run();
            assertEquals(ans, r1);
            assertEquals(ans, r2);
            assertEquals(ans, r3);
        }
    }

    @Test
    public void testStackOverflow() {
        assertThrows(StackOverflowError.class,
            () -> permutation1(0, new int[10000], new boolean[10000], new LinkedList<>(), new ArrayList<>()));
        assertThrows(StackOverflowException.class,
            () -> permutation2(0, new int[10000], new boolean[10000], new LinkedList<>(), new ArrayList<>()).run(1000000));
        assertThrows(StackOverflowException.class,
            () -> permutation3(0, new int[10000], new boolean[10000], new LinkedList<>(), new ArrayList<>()).run(10000));
        assertThrows(StackOverflowException.class,
            () -> permutation4(0, new int[10000], new boolean[10000], new LinkedList<>(), new ArrayList<>()).run(10000));
    }

    @Test
    public void testPermutationIterator() {
        for (int n = 1; n <= 9; n++) {
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = i + 1;
            }

            List<List<Integer>> r = new ArrayList<>();
            permutation1(0, nums, new boolean[n], new LinkedList<>(), r);

            Coroutine co = permutationIterator(0, nums, new boolean[nums.length], new LinkedList<>()).toCoroutine();
            for (List<Integer> p : r) {
                assertEquals(p, co.run());
            }

            assertThrows(EndOfCoroutineException.class, co::run);
        }
    }

    @Test
    public void testNextPermutation2() {
        int[] nums = new int[20];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = i + 1;
        }

        Coroutine co = permutationIterator(0, nums, new boolean[nums.length], new LinkedList<>()).toCoroutine();
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), co.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 19), co.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 18, 20), co.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 18), co.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 18, 19), co.run());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 19, 18), co.run());
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

    private Thunk<Void> permutation2(int index, int[] nums, boolean[] flag, LinkedList<Integer> path, List<List<Integer>> result) {
        if (index == nums.length) {
            result.add(new ArrayList<>(path));
            return empty();
        }

        Thunk<Void> thunk = empty();
        for (int i = 0; i < nums.length; i++) {
            if (!flag[i]) {
                int t = i;
                thunk = thunk.then(() -> flag[t] = true);
                thunk = thunk.then(() -> path.addLast(nums[t]));
                thunk = thunk.then(() -> permutation2(index + 1, nums, flag, path, result));
                thunk = thunk.then(() -> path.removeLast());
                thunk = thunk.then(() -> flag[t] = false);
            }
        }

        return thunk;
    }

    private Thunk<Void> permutation3(int index, int[] nums, boolean[] flag, LinkedList<Integer> path, List<List<Integer>> result) {
        if (index == nums.length) {
            result.add(new ArrayList<>(path));
            return empty();
        }

        int[] i = new int[]{0};
        return loop(
            () -> i[0] < nums.length,
            () -> i[0]++,
            exec(() -> {
                if (!flag[i[0]]) {
                    return exec(() -> flag[i[0]] = true)
                        .then(() -> path.addLast(nums[i[0]]))
                        .then(() -> permutation3(index + 1, nums, flag, path, result))
                        .then(() -> path.removeLast())
                        .then(() -> flag[i[0]] = false);
                }
                return empty();
            })
        );
    }

    private Thunk<Void> permutation4(int index, int[] nums, boolean[] flag, LinkedList<Integer> path, List<List<Integer>> result) {
        if (index == nums.length) {
            result.add(new ArrayList<>(path));
            return empty();
        }

        return iterate(
            Arrays.stream(nums).iterator(),
            (i, n) -> {
                if (!flag[i]) {
                    return exec(() -> flag[i] = true)
                        .then(() -> path.addLast(n))
                        .then(() -> permutation4(index + 1, nums, flag, path, result))
                        .then(() -> path.removeLast())
                        .then(() -> flag[i] = false);
                }
                return empty();
            }
        );
    }

    private Thunk<List<Integer>> permutationIterator(int index, int[] nums, boolean[] flag, LinkedList<Integer> path) {
        if (index == nums.length) {
            return pause(path);
        }

        int[] i = new int[]{0};
        return loop(
            () -> i[0] < nums.length,
            () -> i[0]++,
            exec(() -> {
                if (!flag[i[0]]) {
                    return exec(() -> flag[i[0]] = true)
                        .then(() -> path.addLast(nums[i[0]]))
                        .then(() -> permutationIterator(index + 1, nums, flag, path))
                        .then(() -> path.removeLast())
                        .then(() -> flag[i[0]] = false);
                }
                return empty();
            })
        );
    }
}
