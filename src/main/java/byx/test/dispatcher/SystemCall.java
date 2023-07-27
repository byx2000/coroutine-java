package byx.test.dispatcher;

import byx.test.core.Coroutine;
import byx.test.core.Thunk;

import java.util.function.Consumer;

import static byx.test.core.Thunk.*;

public class SystemCall {
    private final String name;
    private final Object arg;

    public SystemCall(String name, Object arg) {
        this.name = name;
        this.arg = arg;
    }

    public String getName() {
        return name;
    }

    public Object getArg() {
        return arg;
    }

    public static Thunk<Task> createTask(Coroutine coroutine) {
        return pause(new SystemCall("createTask", coroutine));
    }

    public static <T> Thunk<T> waitTask(Task task, Class<T> retType) {
        return waitTask(task);
    }

    public static <T> Thunk<T> waitTask(Task task) {
        return pause(new SystemCall("waitTask", task));
    }

    public static <T> Thunk<T> await(Coroutine coroutine) {
        return pause(new SystemCall("await", coroutine));
    }

    public static <T> Thunk<T> await(Coroutine coroutine, Class<T> retType) {
        return await(coroutine);
    }

    public static <T> Thunk<T> withContinuation(Consumer<Continuation<T>> callback) {
        return pause(new SystemCall("withContinuation", callback));
    }

    public static <T> Thunk<T> withContinuation(Consumer<Continuation<T>> callback, Class<T> retType) {
        return withContinuation(callback);
    }
}
