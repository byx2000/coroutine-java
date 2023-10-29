package byx.trampoline.core;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Trampoline静态工厂方法
 */
public class Trampolines {
    private Trampolines() {}

    /**
     * 空代码段
     */
    public static <T> Trampoline<T> empty() {
        return Empty.getInstance();
    }

    /**
     * 包装单个值
     * @param val val
     */
    public static <T> Trampoline<T> value(T val) {
        return new Value<>(val);
    }

    /**
     * 包装Supplier
     * @param supplier supplier
     */
    public static <T> Trampoline<T> value(Supplier<T> supplier) {
        return empty().flatMap(r -> value(supplier.get()));
    }

    /**
     * 包装Runnable
     * @param runnable runnable
     */
    public static <T> Trampoline<T> exec(Runnable runnable) {
        return empty().flatMap(r -> {
            runnable.run();
            return empty();
        });
    }

    /**
     * 执行supplier生成的Trampoline
     * @param supplier supplier
     */
    public static <T> Trampoline<T> exec(Supplier<Trampoline<T>> supplier) {
        return empty().flatMap(r -> supplier.get());
    }

    /**
     * 循环
     * @param condition 条件
     * @param body 循环体
     */
    public static <T> Trampoline<T> loop(Supplier<Boolean> condition, Trampoline<?> body) {
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
     * @param update 更新
     * @param body 循环体
     */
    public static <T> Trampoline<T> loop(Supplier<Boolean> condition, Runnable update, Trampoline<?> body) {
        return loop(condition, body.then(update));
    }

    /**
     * 循环
     * @param iterable 迭代变量
     * @param bodySupplier 循环体，参数为迭代变量和元素
     */
    public static <T, E> Trampoline<T> loop(Iterable<E> iterable, BiFunction<Integer, E, Trampoline<?>> bodySupplier) {
        Iterator<E> iterator = iterable.iterator();
        AtomicInteger i = new AtomicInteger(0);
        return loop(
            iterator::hasNext,
            i::incrementAndGet,
            exec(() -> bodySupplier.apply(i.get(), iterator.next()))
        );
    }

    /**
     * 循环[startInclusive, endExclusive)
     * @param startInclusive 开始
     * @param endExclusive 结束
     * @param bodySupplier 循环体，参数为迭代变量
     */
    public static <T> Trampoline<T> loop(int startInclusive, int endExclusive, Function<Integer, Trampoline<?>> bodySupplier) {
        AtomicInteger i = new AtomicInteger(startInclusive);
        return loop(
            () -> i.get() < endExclusive,
            i::incrementAndGet,
            exec(() -> bodySupplier.apply(i.get()))
        );
    }

    /**
     * 暂停执行，产生值value
     * @param value value
     */
    public static <T> Trampoline<T> pause(Object value) {
        return new Pause<>(value);
    }

    /**
     * 暂停执行，产生值value，恢复执行时接收的值类型为retType
     * @param value value
     * @param retType retType
     */
    public static <T> Trampoline<T> pause(Object value, Class<T> retType) {
        return new Pause<>(value);
    }

    /**
     * 暂停执行
     */
    public static <T> Trampoline<T> pause() {
        return pause((Object) null);
    }

    /**
     * 暂停执行，恢复执行时接收的值类型为retType
     * @param retType retType
     */
    public static <T> Trampoline<T> pause(Class<T> retType) {
        return pause();
    }

    /**
     * 暂停执行，产生supplier生成的值
     * @param supplier supplier
     */
    public static <T> Trampoline<T> pause(Supplier<Object> supplier) {
        return exec(() -> pause(supplier.get()));
    }

    /**
     * 暂停执行，产生supplier生成的值，恢复执行时接收的值类型为retType
     * @param supplier supplier
     * @param retType retType
     */
    public static <T> Trampoline<T> pause(Supplier<Object> supplier, Class<T> retType) {
        return pause(supplier);
    }
}
