package byx.trampoline.example.recursion;

import byx.trampoline.core.Trampoline;
import byx.trampoline.example.common.TreeNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static byx.trampoline.core.Trampolines.empty;
import static byx.trampoline.core.Trampolines.exec;
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
    public void testStackOverflow() {
        TreeNode root = buildHugeTree();

        assertThrows(StackOverflowError.class, () -> preorderTraverse1(root, new ArrayList<>()));
        assertThrows(StackOverflowError.class, () -> inorderTraverse1(root, new ArrayList<>()));
        assertThrows(StackOverflowError.class, () -> postorderTraverse1(root, new ArrayList<>()));

        assertDoesNotThrow(() -> preorderTraverse2(root, new ArrayList<>()).run());
        assertDoesNotThrow(() -> inorderTraverse2(root, new ArrayList<>()).run());
        assertDoesNotThrow(() -> postorderTraverse2(root, new ArrayList<>()).run());
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
        TreeNode node = new TreeNode(100000);
        for (int i = 99999; i >= 1; i--) {
            node = new TreeNode(i, node, null);
        }
        return node;
    }

    private void preorderTraverse1(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }

        result.add(node.val());
        preorderTraverse1(node.left(), result);
        preorderTraverse1(node.right(), result);
    }

    private Trampoline<Void> preorderTraverse2(TreeNode node, List<Integer> result) {
        if (node == null) {
            return empty();
        }

        return exec(() -> result.add(node.val()))
            .then(() -> preorderTraverse2(node.left(), result))
            .then(() -> preorderTraverse2(node.right(), result));
    }

    private void inorderTraverse1(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }

        inorderTraverse1(node.left(), result);
        result.add(node.val());
        inorderTraverse1(node.right(), result);
    }

    private Trampoline<Void> inorderTraverse2(TreeNode node, List<Integer> result) {
        if (node == null) {
            return empty();
        }

        return exec(() -> inorderTraverse2(node.left(), result))
            .then(() -> result.add(node.val()))
            .then(() -> inorderTraverse2(node.right(), result));
    }

    private void postorderTraverse1(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }

        postorderTraverse1(node.left(), result);
        postorderTraverse1(node.right(), result);
        result.add(node.val());
    }

    private Trampoline<Void> postorderTraverse2(TreeNode node, List<Integer> result) {
        if (node == null) {
            return empty();
        }

        return exec(() -> postorderTraverse2(node.left(), result))
            .then(() -> postorderTraverse2(node.right(), result))
            .then(() -> result.add(node.val()));
    }
}
