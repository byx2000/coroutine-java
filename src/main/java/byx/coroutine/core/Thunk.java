package byx.coroutine.core;

import byx.coroutine.exception.EndOfCoroutineException;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 封装延迟执行的代码片段
 * @param <T> 返回值类型
 */
public interface Thunk<T> {
    /**
     * 空代码段，不执行任何操作
     */
    static <T> Thunk<T> empty() {
        return Empty.getInstance();
    }

    /**
     * 执行runnable包装的代码
     * @param runnable runnable
     */
    static <T> Thunk<T> exec(Runnable runnable) {
        return new FlatMap<>(empty(), r -> {
            runnable.run();
            return empty();
        });
    }

    /**
     * 执行supplier生成的Thunk
     * @param supplier supplier
     */
    static <T> Thunk<T> exec(Supplier<Thunk<T>> supplier) {
        return empty().flatMap(r -> supplier.get());
    }

    /**
     * 返回指定值value
     * @param value value
     */
    static <T> Thunk<T> value(T value) {
        return new Value<>(value);
    }

    /**
     * 返回supplier生成的值
     * @param supplier supplier
     */
    static <T> Thunk<T> value(Supplier<T> supplier) {
        return empty().flatMap(r -> value(supplier.get()));
    }

    /**
     * 暂停当前协程的执行，并产生值value
     * @param value value
     */
    static <T> Thunk<T> pause(Object value) {
        return new Pause<>(value);
    }

    /**
     * 暂停当前协程的执行
     */
    static <T> Thunk<T> pause() {
        return pause(null);
    }

    /**
     * 暂停当前协程的执行，并产生值value，恢复执行时接收的值类型为retType
     * @param value value
     * @param retType retType
     */
    static <T> Thunk<T> pause(Object value, Class<T> retType) {
        return new Pause<>(value);
    }

    /**
     * 暂停当前协程的执行，恢复执行时接收的值类型为retType
     * @param retType retType
     */
    static <T> Thunk<T> pause(Class<T> retType) {
        return pause(null, retType);
    }

    /**
     * 循环
     * @param condition 条件
     * @param body 循环体
     */
    static <T> Thunk<T> loop(Supplier<Boolean> condition, Thunk<?> body) {
        return exec(() -> {
            if (!condition.get()) {
                return empty();
            }
            return body.then(() -> loop(condition, body));
        });
    }

    /**
     * 循环
     * @param condition 条件
     * @param body 循环体
     */
    static <T, U> Thunk<T> loop(Supplier<Boolean> condition, Supplier<Thunk<U>> body) {
        return loop(condition, exec(body));
    }

    /**
     * 循环
     * @param condition 条件
     * @param update 更新
     * @param body 循环体
     */
    static <T> Thunk<T> loop(Supplier<Boolean> condition, Thunk<Void> update, Thunk<?> body) {
        return loop(condition, body.then(update));
    }

    /**
     * 循环
     * @param condition 条件
     * @param update 更新
     * @param body 循环体
     */
    static <T> Thunk<T> loop(Supplier<Boolean> condition, Runnable update, Thunk<?> body) {
        return loop(condition, exec(update), body);
    }

    /**
     * 循环
     * @param condition 条件
     * @param update 更新
     * @param body 循环体
     */
    static <T, U> Thunk<T> loop(Supplier<Boolean> condition, Runnable update, Supplier<Thunk<U>> body) {
        return loop(condition, exec(update), exec(body));
    }

    /**
     * 对iterable进行迭代
     * @param iterable iterable
     * @param body 循环体，参数为迭代变量和元素
     */
    static <T, E> Thunk<T> iterate(Iterable<E> iterable, BiFunction<Integer, E, Thunk<?>> body) {
        return iterate(iterable.iterator(), body);
    }

    /**
     * 对iterator进行迭代
     * @param iterator iterator
     * @param body 循环体，参数为迭代变量和元素
     */
    static <T, E> Thunk<T> iterate(Iterator<E> iterator, BiFunction<Integer, E, Thunk<?>> body) {
        int[] var = new int[]{0};
        return loop(
            iterator::hasNext,
            () -> var[0]++,
            () -> body.apply(var[0], iterator.next())
        );
    }

    /**
     * 对body重复执行指定次数
     * @param times 重复执行的次数
     * @param body 循环体
     */
    static <T> Thunk<T> repeat(int times, Thunk<?> body) {
        int[] var = new int[]{0};
        return loop(
            () -> var[0] < times,
            () -> var[0]++,
            body
        );
    }

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
