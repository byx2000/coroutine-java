package byx.trampoline.example.generator;

import byx.trampoline.core.Generator;
import byx.trampoline.core.Trampoline;
import byx.trampoline.example.common.TreeNode;
import byx.trampoline.exception.EndOfCoroutineException;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static byx.trampoline.core.Trampolines.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BinaryTreeGeneratorTest {
    @Test
    public void testPreorderIterator() {
        TreeNode root = buildTree();
        Generator<Integer> generator = preorder(root).toGenerator();
        assertEquals(List.of(1, 2, 4, 5, 3, 6), generator.stream().toList());
        assertThrows(EndOfCoroutineException.class, () -> preorder(null).toCoroutine().run());
    }

    @Test
    public void testInorderIterator() {
        TreeNode root = buildTree();
        Generator<Integer> generator = inorder(root).toGenerator();
        assertEquals(List.of(4, 2, 5, 1, 3, 6), generator.stream().toList());
        assertThrows(EndOfCoroutineException.class, () -> inorder(null).toCoroutine().run());
    }

    @Test
    public void testPostorderIterator() {
        TreeNode root = buildTree();
        Generator<Integer> generator = postorder(root).toGenerator();
        assertEquals(List.of(4, 5, 2, 6, 3, 1), generator.stream().toList());
        assertThrows(EndOfCoroutineException.class, () -> postorder(null).toCoroutine().run());
    }

    @Test
    public void testLayerOrderIterator() {
        TreeNode root = buildTree();
        Generator<Integer> generator = layerOrder(root).toGenerator();
        assertEquals(List.of(1, 2, 3, 4, 5, 6), generator.stream().toList());
        assertThrows(EndOfCoroutineException.class, () -> layerOrder(null).toCoroutine().run());
    }

    /**
     *     1
     *    / \
     *   2  3
     *  / \  \
     * 4  5   6
     */
    private TreeNode buildTree() {
        return new TreeNode(1, new TreeNode(2, new TreeNode(4), new TreeNode(5)), new TreeNode(3, null, new TreeNode(6)));
    }

    // 前序遍历
    private Trampoline<Void> preorder(TreeNode node) {
        if (node == null) {
            return empty();
        }

        return pause(node.val)
            .then(() -> preorder(node.left))
            .then(() -> preorder(node.right));
    }

    // 中序遍历
    private Trampoline<Void> inorder(TreeNode node) {
        if (node == null) {
            return empty();
        }

        return exec(() -> inorder(node.left))
            .pause(node.val)
            .then(() -> inorder(node.right));
    }

    // 后序遍历
    private Trampoline<Void> postorder(TreeNode node) {
        if (node == null) {
            return empty();
        }

        return exec(() -> postorder(node.left))
            .then(() -> postorder(node.right))
            .pause(node.val);
    }

    // 层序遍历
    private Trampoline<Void> layerOrder(TreeNode node) {
        if (node == null) {
            return empty();
        }

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(node);

        return loop(
            () -> !queue.isEmpty(),
            () -> {
                TreeNode cur = queue.remove();
                if (cur.left != null) {
                    queue.add(cur.left);
                }
                if (cur.right != null) {
                    queue.add(cur.right);
                }
                return pause(cur.val);
            }
        );
    }
}
