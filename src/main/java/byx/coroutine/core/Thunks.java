package byx.coroutine.core;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class Thunks {
    private Thunks() {}

    /**
     * 空代码段，不执行任何操作
     */
    public static <T> Thunk<T> empty() {
        return Empty.getInstance();
    }

    /**
     * 执行runnable包装的代码
     * @param runnable runnable
     */
    public static <T> Thunk<T> exec(Runnable runnable) {
        return new FlatMap<>(empty(), r -> {
            runnable.run();
            return empty();
        });
    }

    /**
     * 执行supplier生成的Thunk
     * @param supplier supplier
     */
    public static <T> Thunk<T> exec(Supplier<Thunk<T>> supplier) {
        return empty().flatMap(r -> supplier.get());
    }

    /**
     * 返回指定值value
     * @param value value
     */
    public static <T> Thunk<T> value(T value) {
        return new Value<>(value);
    }

    /**
     * 返回supplier生成的值
     * @param supplier supplier
     */
    public static <T> Thunk<T> value(Supplier<T> supplier) {
        return empty().flatMap(r -> value(supplier.get()));
    }

    /**
     * 暂停当前协程的执行，并产生值value
     * @param value value
     */
    public static <T> Thunk<T> pause(Object value) {
        return new Pause<>(value);
    }

    /**
     * 暂停当前协程的执行
     */
    public static <T> Thunk<T> pause() {
        return pause(null);
    }

    /**
     * 暂停当前协程的执行，并产生值value，恢复执行时接收的值类型为retType
     * @param value value
     * @param retType retType
     */
    public static <T> Thunk<T> pause(Object value, Class<T> retType) {
        return new Pause<>(value);
    }

    /**
     * 暂停当前协程的执行，恢复执行时接收的值类型为retType
     * @param retType retType
     */
    public static <T> Thunk<T> pause(Class<T> retType) {
        return pause(null, retType);
    }

    /**
     * 循环
     * @param condition 条件
     * @param body 循环体
     */
    public static <T> Thunk<T> loop(Supplier<Boolean> condition, Thunk<?> body) {
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
    public static <T, U> Thunk<T> loop(Supplier<Boolean> condition, Supplier<Thunk<U>> body) {
        return loop(condition, exec(body));
    }

    /**
     * 循环
     * @param condition 条件
     * @param update 更新
     * @param body 循环体
     */
    public static <T> Thunk<T> loop(Supplier<Boolean> condition, Thunk<Void> update, Thunk<?> body) {
        return loop(condition, body.then(update));
    }

    /**
     * 循环
     * @param condition 条件
     * @param update 更新
     * @param body 循环体
     */
    public static <T> Thunk<T> loop(Supplier<Boolean> condition, Runnable update, Thunk<?> body) {
        return loop(condition, exec(update), body);
    }

    /**
     * 循环
     * @param condition 条件
     * @param update 更新
     * @param body 循环体
     */
    public static <T, U> Thunk<T> loop(Supplier<Boolean> condition, Runnable update, Supplier<Thunk<U>> body) {
        return loop(condition, exec(update), exec(body));
    }

    /**
     * 对iterable进行迭代
     * @param iterable iterable
     * @param body 循环体，参数为迭代变量和元素
     */
    public static <T, E> Thunk<T> iterate(Iterable<E> iterable, BiFunction<Integer, E, Thunk<?>> body) {
        return iterate(iterable.iterator(), body);
    }

    /**
     * 对iterator进行迭代
     * @param iterator iterator
     * @param body 循环体，参数为迭代变量和元素
     */
    public static <T, E> Thunk<T> iterate(Iterator<E> iterator, BiFunction<Integer, E, Thunk<?>> body) {
        int[] var = new int[]{0};
        return loop(
            iterator::hasNext,
            () -> var[0]++,
            () -> body.apply(var[0], iterator.next())
        );
    }
}
