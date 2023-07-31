package byx.test.core;

import byx.test.exception.EndOfCoroutineException;
import byx.test.exception.StackOverflowException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

public class Coroutine {
    private final Deque<Object> stack = new ArrayDeque<>();
    private final int maxStackSize;

    public Coroutine(Thunk<?> thunk, int maxStackSize) {
        this.maxStackSize = maxStackSize;
        stack.push(thunk);
    }

    @SuppressWarnings("unchecked")
    public <T> T run(Object value) throws EndOfCoroutineException {
        Object ret = value;
        while (!stack.isEmpty()) {
            if (stack.size() > maxStackSize) {
                throw new StackOverflowException(maxStackSize);
            }

            Object top = stack.peek();
            if (top instanceof Empty) {
                stack.pop();
            } else if (top instanceof Value<?> v) {
                stack.pop();
                ret = v.getValue();
            } else if (top instanceof Pause<?> p) {
                stack.pop();
                return (T) p.getValue();
            } else if (top instanceof FlatMap<?, ?> flatMap) {
                stack.pop();
                stack.push(flatMap.getMapper());
                stack.push(flatMap.getThunk());
            } else if (top instanceof Function mapper) {
                stack.pop();
                stack.push(mapper.apply(ret));
            }
        }

        throw new EndOfCoroutineException(ret);
    }

    public <T> T run() throws EndOfCoroutineException {
        return run(null);
    }
}
