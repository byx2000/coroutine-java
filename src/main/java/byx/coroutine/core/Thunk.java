package byx.coroutine.core;

import byx.coroutine.exception.EndOfCoroutineException;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static byx.coroutine.core.Thunks.loop;
import static byx.coroutine.core.Thunks.value;

/**
 * 封装延迟执行的代码片段
 * @param <T> 返回值类型
 */
public interface Thunk<T> {
    /**
     * 将上一个Thunk的结果映射成一个新的Thunk，并接着执行新的Thunk
     * @param mapper mapper
     */
    default <U> Thunk<U> flatMap(Function<T, Thunk<U>> mapper) {
        return new FlatMap<>(this, mapper);
    }

    /**
     * 将Thunk的结果映射成新的值
     * @param mapper mapper
     */
    default <U> Thunk<U> map(Function<T, U> mapper) {
        return flatMap(r -> value(mapper.apply(r)));
    }

    /**
     * 当前Thunk执行完后，继续执行thunk
     * @param thunk thunk
     */
    default <U> Thunk<U> then(Thunk<U> thunk) {
        return flatMap(r -> thunk);
    }

    /**
     * 当前Thunk执行完后，接着执行runnable
     * @param runnable runnable
     */
    default Thunk<T> then(Runnable runnable) {
        return flatMap(r -> {
            runnable.run();
            return value(r);
        });
    }

    /**
     * 当前Thunk执行完后，继续执行supplier生成的Thunk
     * @param supplier supplier
     */
    default <U> Thunk<U> then(Supplier<Thunk<U>> supplier) {
        return flatMap(r -> supplier.get());
    }

    /**
     * 当前Thunk执行完后，对返回结果进行消费
     * @param consumer consumer
     */
    default Thunk<T> then(Consumer<T> consumer) {
        return flatMap(r -> {
            consumer.accept(r);
            return value(r);
        });
    }

    /**
     * 暂停当前协程的执行
     */
    default <U> Thunk<U> pause() {
        return this.then(Thunks.pause());
    }

    /**
     * 暂停当前协程的执行，并产生值value
     * @param value value
     */
    default <U> Thunk<U> pause(Object value) {
        return this.then(Thunks.pause(value));
    }

    /**
     * 重复执行指定次数
     * @param times 重复次数
     */
    default <U> Thunk<U> repeat(int times) {
        int[] var = new int[]{0};
        return loop(
            () -> var[0] < times,
            () -> var[0]++,
            this
        );
    }

    /**
     * 无限循环
     */
    default <U> Thunk<U> loopForever() {
        return loop(() -> true, this);
    }

    /**
     * 将当前Thunk转换为协程
     */
    default Coroutine toCoroutine() {
        return toCoroutine(Integer.MAX_VALUE);
    }

    /**
     * 将当前Thunk转换为协程
     * @param maxStackSize 最大栈容量
     */
    default Coroutine toCoroutine(int maxStackSize) {
        return new Coroutine(this, maxStackSize);
    }

    /**
     * 直接运行当前Thunk
     */
    default T run() {
        return run(Integer.MAX_VALUE);
    }

    /**
     * 直接运行当前Thunk
     * @param maxStackSize 最大栈容量
     */
    @SuppressWarnings("unchecked")
    default T run(int maxStackSize) {
        Coroutine co = toCoroutine(maxStackSize);
        while (true) {
            try {
                co.run();
            } catch (EndOfCoroutineException e) {
                return (T) e.getRetVal();
            }
        }
    }
}
