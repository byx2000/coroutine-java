package byx.test;

import byx.test.core.Thunk;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static byx.test.core.Thunk.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CombinationTest {
    @Test
    public void testCombination() {
        List<List<Integer>> r1 = new ArrayList<>();
        dfs(new int[]{2, 3, 6, 7}, 7, 0, new LinkedList<>(), r1).run();
        assertEquals(List.of(
            List.of(7),
            List.of(2, 2, 3)
        ), r1);

        List<List<Integer>> r2 = new ArrayList<>();
        dfs(new int[]{2, 3, 5}, 8, 0, new LinkedList<>(), r2).run();
        assertEquals(List.of(
            List.of(3, 5),
            List.of(2, 3, 3),
            List.of(2, 2, 2, 2)
        ), r2);
    }

    private Thunk<Void> dfs(int[] nums, int sum, int index, LinkedList<Integer> path, List<List<Integer>> res) {
        if (index == nums.length) {
            if (sum == 0) {
                res.add(new ArrayList<>(path));
            }
            return empty();
        }

        Thunk<Void> thunk = exec(() -> dfs(nums, sum, index + 1, path, res));
        if (sum >= nums[index]) {
            thunk = thunk.then(() -> path.addLast(nums[index]))
                .then(() -> dfs(nums, sum - nums[index], index, path, res))
                .then(() -> path.removeLast());
        }

        return thunk;
    }
}
