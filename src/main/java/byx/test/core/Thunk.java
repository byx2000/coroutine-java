package byx.test.core;

import byx.test.exception.EndOfCoroutineException;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 封装延迟执行的代码片段
 *
 * @param <T> 返回类型
 */
public interface Thunk<T> {
    static <T> Thunk<T> empty() {
        return Empty.getInstance();
    }

    static <T> Thunk<T> exec(Runnable runnable) {
        return new FlatMap<>(empty(), r -> {
            runnable.run();
            return empty();
        });
    }

    static <T> Thunk<T> exec(Supplier<Thunk<T>> supplier) {
        return empty().flatMap(r -> supplier.get());
    }

    static <T> Thunk<T> value(T value) {
        return new Value<>(value);
    }

    static <T> Thunk<T> pause(Object value) {
        return new Pause<>(value);
    }

    static <T> Thunk<T> pause() {
        return pause(null);
    }

    static <T> Thunk<T> pause(Object value, Class<T> retType) {
        return new Pause<>(value);
    }

    static <T> Thunk<T> pause(Class<T> retType) {
        return pause(null, retType);
    }

    static <T> Thunk<T> loop(Supplier<Boolean> condition, Thunk<?> body) {
        if (!condition.get()) {
            return empty();
        }
        return body.then(() -> loop(condition, body));
    }

    static <T, U> Thunk<T> loop(Supplier<Boolean> condition, Supplier<Thunk<U>> body) {
        return loop(condition, exec(body));
    }

    static <T> Thunk<T> loop(Supplier<Boolean> condition, Thunk<Void> update, Thunk<?> body) {
        return loop(condition, body.then(update));
    }

    static <T> Thunk<T> loop(Supplier<Boolean> condition, Runnable update, Thunk<?> body) {
        return loop(condition, exec(update), body);
    }

    static <T, U> Thunk<T> loop(Supplier<Boolean> condition, Runnable update, Supplier<Thunk<U>> body) {
        return loop(condition, exec(update), exec(body));
    }

    static <T> Thunk<T> repeat(int times, Thunk<?> body) {
        int[] var = new int[]{0};
        return loop(
            () -> var[0] < times,
            () -> var[0]++,
            body
        );
    }

    default <U> Thunk<U> flatMap(Function<T, Thunk<U>> mapper) {
        return new FlatMap<>(this, mapper);
    }

    default <U> Thunk<U> map(Function<T, U> mapper) {
        return flatMap(r -> value(mapper.apply(r)));
    }

    default <U> Thunk<U> then(Thunk<U> thunk) {
        return flatMap(r -> thunk);
    }

    default Thunk<T> then(Runnable runnable) {
        return flatMap(r -> {
            runnable.run();
            return empty();
        });
    }

    default <U> Thunk<U> then(Supplier<Thunk<U>> supplier) {
        return flatMap(r -> supplier.get());
    }

    default Thunk<T> then(Consumer<T> consumer) {
        return flatMap(r -> {
            consumer.accept(r);
            return empty();
        });
    }

    default Coroutine toCoroutine() {
        return toCoroutine(Integer.MAX_VALUE);
    }

    default Coroutine toCoroutine(int maxStackSize) {
        return new Coroutine(this, maxStackSize);
    }

    @SuppressWarnings("unchecked")
    default T run(int maxStackSize) {
        Coroutine co = toCoroutine(maxStackSize);
        Object ret;

        try {
            while (true) {
                co.run();
            }
        } catch (EndOfCoroutineException e) {
            ret = e.getRetVal();
        }

        return (T) ret;
    }

    default T run() {
        return run(Integer.MAX_VALUE);
    }
}
