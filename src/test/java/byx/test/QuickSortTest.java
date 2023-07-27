package byx.test;

import byx.test.core.Thunk;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static byx.test.core.Thunk.*;
import static org.junit.jupiter.api.Assertions.*;

public class QuickSortTest {
    @Test
    public void testQuickSort() {
        int[] ans = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int i = 0; i < 100; ++i) {
            List<Integer> list = new ArrayList<>(Arrays.stream(ans).boxed().toList());
            Collections.shuffle(list);

            int[] nums1 = list.stream().mapToInt(n -> n).toArray();
            qsort1(nums1, 0, nums1.length - 1);
            assertArrayEquals(ans, nums1);

            int[] nums2 = list.stream().mapToInt(n -> n).toArray();
            qsort2(nums2, 0, nums2.length - 1).run();
            assertArrayEquals(ans, nums2);
        }
    }

    @Test
    public void testStackOverflow() {
        int[] nums = new int[10000];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = 10000 - i;
        }

        assertThrows(StackOverflowError.class, () -> qsort1(nums, 0, nums.length - 1));
        qsort2(nums, 0, nums.length - 1).run();
        for (int i = 0; i < nums.length; i++) {
            assertEquals(i + 1, nums[i]);
        }
    }

    private void swap(int[] nums, int i, int j) {
        int t = nums[i];
        nums[i] = nums[j];
        nums[j] = t;
    }

    private int partition(int[] nums, int left, int right) {
        int i = left + 1, j = right;
        while (i <= j) {
            while (i <= j && nums[i] <= nums[left]) i++;
            while (i <= j && nums[j] > nums[left]) j--;
            if (i < j) {
                swap(nums, i, j);
            }
        }
        swap(nums, left, j);
        return j;
    }

    private void qsort1(int[] nums, int left, int right) {
        if (left >= right) {
            return;
        }

        int mid = partition(nums, left, right);
        qsort1(nums, left, mid - 1);
        qsort1(nums, mid + 1, right);
    }

    private Thunk<Void> qsort2(int[] nums, int left, int right) {
        if (left >= right) {
            return empty();
        }

        int mid = partition(nums, left, right);
        return exec(() -> qsort2(nums, left, mid - 1))
            .then(() -> qsort2(nums, mid + 1, right));
    }
}
