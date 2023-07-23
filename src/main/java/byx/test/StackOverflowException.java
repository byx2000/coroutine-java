package byx.test;

public class StackOverflowException extends RuntimeException {
    public StackOverflowException(int maxStackSize) {
        super("stack size exceed " + maxStackSize);
    }
}
