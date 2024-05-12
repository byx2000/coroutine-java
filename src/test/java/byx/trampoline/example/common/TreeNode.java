package byx.trampoline.example.common;

public record TreeNode(int val, TreeNode left, TreeNode right) {
    public TreeNode(int val) {
        this(val, null, null);
    }
}