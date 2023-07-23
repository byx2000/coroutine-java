package byx.test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 封装延迟执行的代码片段
 *
 * @param <T> 返回类型
 */
public abstract class Thunk<T> {
    private static class Empty<T> extends Thunk<T> {
        private static final Empty<?> INSTANCE = new Empty<>();

        private Empty() {
        }
    }

    private static class Value<T> extends Thunk<T> {
        private final T value;

        private Value(T value) {
            this.value = value;
        }
    }

    public static class Pause<T, U> extends Thunk<T> {
        private final U value;

        private Pause(U value) {
            this.value = value;
        }
    }

    private static class FlatMap<T, U> extends Thunk<U> {
        private final Thunk<T> thunk;
        private final Function<T, Thunk<U>> mapper;

        private FlatMap(Thunk<T> thunk, Function<T, Thunk<U>> mapper) {
            this.thunk = thunk;
            this.mapper = mapper;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Thunk<T> empty() {
        return (Thunk<T>) Empty.INSTANCE;
    }

    public static <T> Thunk<T> exec(Runnable runnable) {
        return new FlatMap<>(empty(), r -> {
            runnable.run();
            return empty();
        });
    }

    public static <T> Thunk<T> exec(Supplier<Thunk<T>> supplier) {
        return empty().flatMap(r -> supplier.get());
    }

    public static <T> Thunk<T> value(T value) {
        return new Value<>(value);
    }

    public static <T> Thunk<T> pause(Object value) {
        return new Pause<>(value);
    }

    public static <T, U> Thunk<U> pause(T value, Class<U> retType) {
        return new Pause<>(value);
    }

    public static <T> Thunk<T> pause(Class<T> retType) {
        return pause(null, retType);
    }

    public static <T> Thunk<T> loop(Supplier<Boolean> condition, Thunk<?> body) {
        if (!condition.get()) {
            return empty();
        }
        return body.then(() -> loop(condition, body));
    }

    public static <T, U> Thunk<T> loop(Supplier<Boolean> condition, Supplier<Thunk<U>> body) {
        return loop(condition, exec(body));
    }

    public static <T> Thunk<T> loop(Supplier<Boolean> condition, Thunk<Void> update, Thunk<?> body) {
        return loop(condition, body.then(update));
    }

    public static <T> Thunk<T> loop(Supplier<Boolean> condition, Runnable update, Thunk<?> body) {
        return loop(condition, exec(update), body);
    }

    public static <T, U> Thunk<T> loop(Supplier<Boolean> condition, Runnable update, Supplier<Thunk<U>> body) {
        return loop(condition, exec(update), exec(body));
    }

    public <U> Thunk<U> flatMap(Function<T, Thunk<U>> mapper) {
        return new FlatMap<>(this, mapper);
    }

    public <U> Thunk<U> map(Function<T, U> mapper) {
        return flatMap(r -> value(mapper.apply(r)));
    }

    public <U> Thunk<U> then(Thunk<U> thunk) {
        return flatMap(r -> thunk);
    }

    public Thunk<T> then(Runnable runnable) {
        return flatMap(r -> {
            runnable.run();
            return value(r);
        });
    }

    public <U> Thunk<U> then(Supplier<Thunk<U>> supplier) {
        return flatMap(r -> supplier.get());
    }

    public Thunk<T> then(Consumer<T> consumer) {
        return flatMap(r -> {
            consumer.accept(r);
            return value(r);
        });
    }

    private static class Frame {
        private Thunk<?> thunk;
        private byte flag;

        private Frame(Thunk<?> thunk) {
            this.thunk = thunk;
            this.flag = 0;
        }
    }

    @SuppressWarnings("unchecked")
    public Coroutine toCoroutine() {
        Deque<Frame> stack = new ArrayDeque<>();
        AtomicReference<Object> ret = new AtomicReference<>(null);
        stack.push(new Frame(this));

        return value -> {
            ret.set(value);
            return (T) runStack(stack, ret, Integer.MAX_VALUE);
        };
    }

    @SuppressWarnings("unchecked")
    public T run(int maxStackSize) {
        Deque<Frame> stack = new ArrayDeque<>();
        AtomicReference<Object> ret = new AtomicReference<>(null);
        stack.push(new Frame(this));

        try {
            runStack(stack, ret, maxStackSize);
        } catch (EndOfCoroutineException ignored) {

        }

        return (T) ret.get();
    }

    public T run() {
        return run(Integer.MAX_VALUE);
    }

    private Object runStack(Deque<Frame> stack, AtomicReference<Object> ret, int maxStackSize) {
        while (!stack.isEmpty()) {
            if (stack.size() > maxStackSize) {
                throw new StackOverflowException(maxStackSize);
            }

            Frame top = stack.peek();
            Thunk<?> thunk = top.thunk;
            if (thunk instanceof Empty) {
                stack.pop();
            } else if (thunk instanceof Value<?> v) {
                stack.pop();
                ret.set(v.value);
            } else if (thunk instanceof Pause<?, ?> p) {
                stack.pop();
                return p.value;
            } else if (thunk instanceof FlatMap flatMap) {
                if (top.flag == 0) {
                    top.flag = 1;
                    stack.push(new Frame(flatMap.thunk));
                } else {
                    stack.pop();
                    stack.push(new Frame((Thunk<?>) flatMap.mapper.apply(ret.get())));
                }
            }
        }

        throw new EndOfCoroutineException();
    }
}
