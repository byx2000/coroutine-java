package byx.test.core;

import byx.test.exception.EndOfCoroutineException;
import byx.test.exception.StackOverflowException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/**
 * 协程
 */
public class Coroutine {
    private final Deque<Object> stack = new ArrayDeque<>();
    private final int maxStackSize;

    /**
     * 创建协程
     * @param thunk 代码段
     * @param maxStackSize 最大栈容量
     */
    public Coroutine(Thunk<?> thunk, int maxStackSize) {
        this.maxStackSize = maxStackSize;
        stack.push(thunk);
    }

    /**
     * 运行协程
     * @param value 发送给协程的值
     * @return 协程暂停时产生的值
     * @throws EndOfCoroutineException 协程运行完毕后抛出此异常
     */
    @SuppressWarnings("unchecked")
    public <T> T run(Object value) throws EndOfCoroutineException {
        Object ret = value;
        while (!stack.isEmpty()) {
            if (stack.size() > maxStackSize) {
                throw new StackOverflowException(maxStackSize);
            }

            Object top = stack.peek();
            if (top instanceof Empty) {
                ret = null;
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

    /**
     * 运行协程，默认发送null
     * @return 协程暂停时产生的值
     * @throws EndOfCoroutineException 协程运行完毕后抛出此异常
     */
    public <T> T run() throws EndOfCoroutineException {
        return run(null);
    }
}
