package byx.trampoline.core;

import byx.trampoline.exception.EndOfCoroutineException;
import byx.trampoline.exception.StackOverflowException;

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
     * @param trampoline trampoline
     * @param maxStackSize 最大栈容量
     */
    public Coroutine(Trampoline<?> trampoline, int maxStackSize) {
        this.maxStackSize = maxStackSize;
        stack.push(trampoline);
    }

    /**
     * 运行协程
     * @param value 发送给协程的值
     * @return 协程暂停时产生的值
     * @throws EndOfCoroutineException 协程运行完毕后抛出此异常
     */
    @SuppressWarnings("unchecked")
    public <T> T run(Object value) throws EndOfCoroutineException {
        Object ret = value; // 返回值
        while (!stack.isEmpty()) {
            // 超出最大栈容量，抛出异常
            if (stack.size() > maxStackSize) {
                throw new StackOverflowException(maxStackSize);
            }

            Object top = stack.pop();
            if (top instanceof Empty) {
                // 空代码段，不执行任何操作，返回值为null
                ret = null;
            } else if (top instanceof Value<?> v) {
                // 返回值设置为指定值
                ret = v.getValue();
            } else if (top instanceof Pause<?> p) {
                // 协程暂停，返回产生的值
                return (T) p.getValue();
            } else if (top instanceof FlatMap<?, ?> flatMap) {
                // 先对mapper入栈，再对head入栈，这样会先执行head，再执行mapper
                stack.push(flatMap.getMapper());
                stack.push(flatMap.getHead());
            } else if (top instanceof Function mapper) {
                // 遇到之前执行FlatMap时插入的mapper，调用mapper.apply产生一个新的Trampoline并入栈
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
