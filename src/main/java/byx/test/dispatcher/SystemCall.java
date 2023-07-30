package byx.test.dispatcher;

import byx.test.core.Coroutine;
import byx.test.core.Thunk;

import java.util.function.Consumer;

import static byx.test.core.Thunk.*;

public class SystemCall {
    public static final String CREATE_TASK = "createTask";
    public static final String WAIT = "wait";
    public static final String AWAIT = "await";
    public static final String WITH_CONTINUATION = "withContinuation";

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
        return pause(new SystemCall(CREATE_TASK, coroutine));
    }

    public static <T> Thunk<T> wait(Task task, Class<T> retType) {
        return wait(task);
    }

    public static <T> Thunk<T> wait(Task task) {
        return pause(new SystemCall(WAIT, task));
    }

    public static <T> Thunk<T> await(Coroutine coroutine) {
        return pause(new SystemCall(AWAIT, coroutine));
    }

    public static <T> Thunk<T> await(Coroutine coroutine, Class<T> retType) {
        return await(coroutine);
    }

    public static <T> Thunk<T> withContinuation(Consumer<Continuation> callback) {
        return pause(new SystemCall(WITH_CONTINUATION, callback));
    }

    public static <T> Thunk<T> withContinuation(Consumer<Continuation> callback, Class<T> retType) {
        return withContinuation(callback);
    }
}
