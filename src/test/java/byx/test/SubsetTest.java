package byx.test;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static byx.test.Thunk.*;
import static org.junit.jupiter.api.Assertions.*;

public class SubsetTest {
    @Test
    public void testSubset() {
        for (int n = 1; n <= 10; n++) {
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = i + 1;
            }

            List<List<Integer>> ans = new ArrayList<>();
            subset1(nums, 0, new LinkedList<>(), ans);
            List<List<Integer>> r = new ArrayList<>();
            subset2(nums, 0, new LinkedList<>(), r).run();
            assertEquals(ans, r);
        }
    }

    @Test
    public void testStackOverflow() {
        int[] nums = new int[10000];
        assertThrows(StackOverflowError.class, () -> subset1(nums, 0, new LinkedList<>(), new ArrayList<>()));
        assertThrows(StackOverflowException.class, () -> subset2(nums, 0, new LinkedList<>(), new ArrayList<>()).run(10000));
    }

    @Test
    public void testSubsetIterator1() {
        for (int n = 1; n <= 10; n++) {
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = i + 1;
            }

            List<List<Integer>> ans = new ArrayList<>();
            subset1(nums, 0, new LinkedList<>(), ans);

            Coroutine co = subsetIterator(nums, 0, new LinkedList<>()).toCoroutine();
            for (List<Integer> set : ans) {
                assertEquals(set, co.run());
            }

            assertThrows(EndOfCoroutineException.class, co::run);
        }
    }

    @Test
    public void testSubsetIterator2() {
        int[] nums = new int[50];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = i + 1;
        }

        Coroutine co = subsetIterator(nums, 0, new LinkedList<>()).toCoroutine();
        assertEquals(List.of(), co.run());
        assertEquals(List.of(50), co.run());
        assertEquals(List.of(49), co.run());
        assertEquals(List.of(49, 50), co.run());
        assertEquals(List.of(48), co.run());
        assertEquals(List.of(48, 50), co.run());
        assertEquals(List.of(48, 49), co.run());
        assertEquals(List.of(48, 49, 50), co.run());
        assertEquals(List.of(47), co.run());
        assertEquals(List.of(47, 50), co.run());
    }

    private void subset1(int[] nums, int index, LinkedList<Integer> current, List<List<Integer>> result) {
        if (index == nums.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        subset1(nums, index + 1, current, result);
        current.add(nums[index]);
        subset1(nums, index + 1, current, result);
        current.removeLast();
    }

    private Thunk<Void> subset2(int[] nums, int index, LinkedList<Integer> current, List<List<Integer>> result) {
        if (index == nums.length) {
            result.add(new ArrayList<>(current));
            return empty();
        }

        return exec(() -> subset2(nums, index + 1, current, result))
            .then(() -> current.add(nums[index]))
            .then(() -> subset2(nums, index + 1, current, result))
            .then(() -> current.removeLast());
    }

    private Thunk<List<Integer>> subsetIterator(int[] nums, int index, LinkedList<Integer> current) {
        if (index == nums.length) {
            return pause(current);
        }

        return exec(() -> subsetIterator(nums, index + 1, current))
            .then(() -> current.add(nums[index]))
            .then(() -> subsetIterator(nums, index + 1, current))
            .then(() -> current.removeLast());
    }
}
