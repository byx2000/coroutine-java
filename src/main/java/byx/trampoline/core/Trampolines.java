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

    private static final Trampoline<?> EMPTY = new Value<>(null);

    /**
     * 空代码段
     */
    @SuppressWarnings("unchecked")
    public static <T> Trampoline<T> empty() {
        return (Trampoline<T>) EMPTY;
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
     * @param cond 条件
     * @param body 循环体
     */
    public static Trampoline<Void> loop(Trampoline<Boolean> cond, Trampoline<?> body) {
        return cond.flatMap(b -> {
            if (b) {
                return body.then(() -> loop(cond, body));
            } else {
                return empty();
            }
        });
    }

    /**
     * 循环
     * @param condSupplier 条件
     * @param bodySupplier 循环体
     */
    public static Trampoline<Void> loop(Supplier<Boolean> condSupplier, Supplier<Trampoline<?>> bodySupplier) {
        return loop(value(condSupplier), exec(bodySupplier::get));
    }

    /**
     * 循环
     * @param condSupplier 条件
     * @param body 循环体
     */
    public static Trampoline<Void> loop(Supplier<Boolean> condSupplier, Runnable body) {
        return loop(value(condSupplier), exec(body));
    }

    /**
     * 循环
     * @param iterable 迭代变量
     * @param bodySupplier 循环体，参数为迭代变量和元素
     */
    public static <E> Trampoline<Void> loop(Iterable<E> iterable, BiFunction<Integer, E, Trampoline<?>> bodySupplier) {
        Iterator<E> iterator = iterable.iterator();
        AtomicInteger i = new AtomicInteger(0);
        return loop(
            iterator::hasNext,
            () -> bodySupplier.apply(i.get(), iterator.next())
                .then(i::incrementAndGet)
        );
    }

    /**
     * 循环[startInclusive, endExclusive)
     * @param startInclusive 开始
     * @param endExclusive 结束
     * @param bodySupplier 循环体，参数为迭代变量
     */
    public static Trampoline<Void> loop(int startInclusive, int endExclusive, Function<Integer, Trampoline<?>> bodySupplier) {
        AtomicInteger i = new AtomicInteger(startInclusive);
        return loop(
            () -> i.get() < endExclusive,
            () -> bodySupplier.apply(i.get())
                .then(i::incrementAndGet)
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
}
