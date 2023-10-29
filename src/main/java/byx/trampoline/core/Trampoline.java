package byx.trampoline.core;

import byx.trampoline.exception.EndOfCoroutineException;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * 封装延迟执行的代码片段
 * @param <T> 返回值类型
 */
public interface Trampoline<T> {
    /**
     * 将当前Trampoline的结果映射成一个新的Trampoline，并接着执行新的Trampoline
     * @param mapper mapper
     */
    default <U> Trampoline<U> flatMap(Function<T, Trampoline<U>> mapper) {
        return new FlatMap<>(this, mapper);
    }

    /**
     * 转换当前Trampoline的结果
     * @param mapper mapper
     */
    default <U> Trampoline<U> map(Function<T, U> mapper) {
        return flatMap(r -> Trampolines.value(mapper.apply(r)));
    }

    /**
     * 当前Trampoline执行完后，继续执行下一个Trampoline
     * @param trampoline trampoline
     */
    default <U> Trampoline<U> then(Trampoline<U> trampoline) {
        return flatMap(r -> trampoline);
    }

    /**
     * 当前Trampoline执行完后，接着执行runnable，并传递当前Trampoline的结果
     * @param runnable runnable
     */
    default Trampoline<T> then(Runnable runnable) {
        return flatMap(r -> {
            runnable.run();
            return Trampolines.value(r);
        });
    }

    /**
     * 当前Trampoline执行完后，继续执行supplier生成的Trampoline
     * @param supplier supplier
     */
    default <U> Trampoline<U> then(Supplier<Trampoline<U>> supplier) {
        return flatMap(r -> supplier.get());
    }

    /**
     * 当前Trampoline执行完后，对返回结果进行消费，并传递当前Trampoline的结果
     * @param consumer consumer
     */
    default Trampoline<T> then(Consumer<T> consumer) {
        return flatMap(r -> {
            consumer.accept(r);
            return Trampolines.value(r);
        });
    }

    /**
     * 后接value(val)
     * @param val val
     */
    default <U> Trampoline<U> value(U val) {
        return this.then(Trampolines.value(val));
    }

    /**
     * 后接value(supplier)
     * @param supplier supplier
     */
    default <U> Trampoline<U> value(Supplier<U> supplier) {
        return this.then(Trampolines.value(supplier));
    }

    /**
     * 后接loop
     * @param condition condition
     * @param body body
     */
    default <U> Trampoline<U> loop(Supplier<Boolean> condition, Trampoline<?> body) {
        return this.then(Trampolines.loop(condition, body));
    }

    /**
     * 重复执行指定次数
     * @param times 重复次数
     */
    default <U> Trampoline<U> repeat(int times) {
        return Trampolines.loop(0, times, i -> this);
    }

    /**
     * 暂停执行，产生值value
     * @param value value
     */
    default <U> Trampoline<U> pause(Object value) {
        return this.then(Trampolines.pause(value));
    }

    /**
     * 暂停执行，产生值value，恢复执行时接收的值类型为retType
     * @param value value
     * @param retType retType
     */
    default <U> Trampoline<U> pause(Object value, Class<U> retType) {
        return this.then(Trampolines.pause(value, retType));
    }

    /**
     * 暂停执行
     */
    default <U> Trampoline<U> pause() {
        return this.then(Trampolines.pause());
    }

    /**
     * 暂停执行，恢复执行时接收的值类型为retType
     * @param retType retType
     */
    default <U> Trampoline<U> pause(Class<U> retType) {
        return this.then(Trampolines.pause(retType));
    }

    /**
     * 暂停执行，产生supplier生成的值
     * @param supplier supplier
     */
    default <U> Trampoline<U> pause(Supplier<Object> supplier) {
        return this.then(Trampolines.pause(supplier));
    }

    /**
     * 暂停执行，产生supplier生成的值，恢复执行时接收的值类型为retType
     * @param supplier supplier
     * @param retType retType
     */
    default <U> Trampoline<U> pause(Supplier<Object> supplier, Class<U> retType) {
        return this.then(Trampolines.pause(supplier, retType));
    }

    /**
     * 将当前Trampoline转换为协程
     */
    default Coroutine toCoroutine() {
        return toCoroutine(Integer.MAX_VALUE);
    }

    /**
     * 将当前Trampoline转换为协程
     * @param maxStackSize 最大栈容量
     */
    default Coroutine toCoroutine(int maxStackSize) {
        return new Coroutine(this, maxStackSize);
    }

    /**
     * 运行当前Trampoline
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

    /**
     * 运行当前Trampoline
     */
    default T run() {
        return run(Integer.MAX_VALUE);
    }
}
