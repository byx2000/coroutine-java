package byx.coroutine;

import byx.coroutine.core.Coroutine;
import byx.coroutine.core.Thunk;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static byx.coroutine.core.Thunks.*;
import static org.junit.jupiter.api.Assertions.*;

public class BinaryTreeTest {
    @Test
    public void testPreorderTraverse() {
        TreeNode root = buildTree();
        List<Integer> r1 = new ArrayList<>();
        preorderTraverse1(root, r1);
        List<Integer> r2 = new ArrayList<>();
        preorderTraverse2(root, r2).run();
        assertEquals(r1, r2);
    }

    @Test
    public void testInorderTraverse() {
        TreeNode root = buildTree();
        List<Integer> r1 = new ArrayList<>();
        inorderTraverse1(root, r1);
        List<Integer> r2 = new ArrayList<>();
        inorderTraverse2(root, r2).run();
        assertEquals(r1, r2);
    }

    @Test
    public void testPostOrderTraverse() {
        TreeNode root = buildTree();
        List<Integer> r1 = new ArrayList<>();
        postorderTraverse1(root, r1);
        List<Integer> r2 = new ArrayList<>();
        postorderTraverse2(root, r2).run();
        assertEquals(r1, r2);
    }

    @Test
    public void testTraverse() {
        TreeNode root = buildTree();
        List<String> output1 = new ArrayList<>();
        traverse1(root, output1);
        List<String> output2 = new ArrayList<>();
        traverse2(root, output2).run();
        assertEquals(output1, output2);
    }

    @Test
    public void testStackOverflow() {
        TreeNode root = buildHugeTree();

        assertThrows(StackOverflowError.class, () -> preorderTraverse1(root, new ArrayList<>()));
        assertThrows(StackOverflowError.class, () -> inorderTraverse1(root, new ArrayList<>()));
        assertThrows(StackOverflowError.class, () -> postorderTraverse1(root, new ArrayList<>()));
        assertThrows(StackOverflowError.class, () -> traverse1(root, new ArrayList<>()));

        assertDoesNotThrow(() -> preorderTraverse2(root, new ArrayList<>()).run());
        assertDoesNotThrow(() -> inorderTraverse2(root, new ArrayList<>()).run());
        assertDoesNotThrow(() -> postorderTraverse2(root, new ArrayList<>()).run());
        assertDoesNotThrow(() -> traverse2(root, new ArrayList<>()).run());
    }

    @Test
    public void testPreorderIterator() {
        TreeNode root = buildTree();
        Coroutine co = preorderIterator(root).toCoroutine();
        assertEquals(1, (int) co.run());
        assertEquals(2, (int) co.run());
        assertEquals(4, (int) co.run());
        assertEquals(5, (int) co.run());
        assertEquals(3, (int) co.run());
        assertEquals(6, (int) co.run());
    }

    @Test
    public void testInorderIterator() {
        TreeNode root = buildTree();
        Coroutine co = inorderIterator(root).toCoroutine();
        assertEquals(4, (int) co.run());
        assertEquals(2, (int) co.run());
        assertEquals(5, (int) co.run());
        assertEquals(1, (int) co.run());
        assertEquals(3, (int) co.run());
        assertEquals(6, (int) co.run());
    }

    @Test
    public void testPostorderIterator() {
        TreeNode root = buildTree();
        Coroutine co = postorderIterator(root).toCoroutine();
        assertEquals(4, (int) co.run());
        assertEquals(5, (int) co.run());
        assertEquals(2, (int) co.run());
        assertEquals(6, (int) co.run());
        assertEquals(3, (int) co.run());
        assertEquals(1, (int) co.run());
    }

    /**
     *      1
     *     / \
     *    2  3
     *   / \  \
     *  4  5  6
     */
    private TreeNode buildTree() {
        return new TreeNode(1, new TreeNode(2, new TreeNode(4), new TreeNode(5)), new TreeNode(3, null, new TreeNode(6)));
    }

    private void preorderTraverse1(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }

        result.add(node.val);
        preorderTraverse1(node.left, result);
        preorderTraverse1(node.right, result);
    }

    private Thunk<Void> preorderTraverse2(TreeNode node, List<Integer> result) {
        if (node == null) {
            return empty();
        }

        result.add(node.val);
        return exec(() -> preorderTraverse2(node.left, result))
            .then(() -> preorderTraverse2(node.right, result));
    }

    private void inorderTraverse1(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }

        inorderTraverse1(node.left, result);
        result.add(node.val);
        inorderTraverse1(node.right, result);
    }

    private Thunk<Void> inorderTraverse2(TreeNode node, List<Integer> result) {
        if (node == null) {
            return empty();
        }

        return exec(() -> inorderTraverse2(node.left, result))
            .then(() -> result.add(node.val))
            .then(() -> inorderTraverse2(node.right, result));
    }

    private void postorderTraverse1(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }

        postorderTraverse1(node.left, result);
        postorderTraverse1(node.right, result);
        result.add(node.val);
    }

    private Thunk<Void> postorderTraverse2(TreeNode node, List<Integer> result) {
        if (node == null) {
            return empty();
        }

        return exec(() -> postorderTraverse2(node.left, result))
            .then(() -> postorderTraverse2(node.right, result))
            .then(() -> result.add(node.val));
    }

    private void traverse1(TreeNode node, List<String> output) {
        if (node == null) {
            return;
        }

        output.add(String.format("before %d left", node.val));
        traverse1(node.left, output);
        output.add(String.format("after %d left", node.val));
        output.add(String.format("before %d right", node.val));
        traverse1(node.right, output);
        output.add(String.format("after %d right", node.val));
    }

    private Thunk<Void> traverse2(TreeNode node, List<String> output) {
        if (node == null) {
            return empty();
        }

        output.add(String.format("before %d left", node.val));
        return exec(() -> traverse2(node.left, output))
            .then(() -> {
                output.add(String.format("after %d left", node.val));
                output.add(String.format("before %d right", node.val));
            })
            .then(() -> traverse2(node.right, output))
            .then(() -> output.add(String.format("after %d right", node.val)));
    }

    /**
     *         1
     *        /
     *       2
     *      /
     *     3
     *    /
     *  ...
     */
    private TreeNode buildHugeTree() {
        TreeNode root = new TreeNode(1);
        TreeNode n = root;
        for (int i = 2; i <= 100000; i++) {
            n.left = new TreeNode(i);
            n = n.left;
        }
        return root;
    }

    private Thunk<Integer> preorderIterator(TreeNode node) {
        if (node == null) {
            return empty();
        }

        return pause(node.val)
            .then(() -> preorderIterator(node.left))
            .then(() -> preorderIterator(node.right));
    }

    private Thunk<Integer> inorderIterator(TreeNode node) {
        if (node == null) {
            return empty();
        }

        return exec(() -> inorderIterator(node.left))
            .pause(node.val)
            .then(() -> inorderIterator(node.right));
    }

    private Thunk<Integer> postorderIterator(TreeNode node) {
        if (node == null) {
            return empty();
        }

        return exec(() -> postorderIterator(node.left))
            .then(() -> postorderIterator(node.right))
            .pause(node.val);
    }
}

class TreeNode {
    public int val;
    public TreeNode left, right;

    public TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }

    public TreeNode(int val) {
        this(val, null, null);
    }
}
