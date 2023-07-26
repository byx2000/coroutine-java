package byx.test;

import static byx.test.Thunk.*;

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

    public static Thunk<Long> getTid() {
        return pause(new SystemCall("getTid", null));
    }

    public static Thunk<Long> newTask(Coroutine coroutine) {
        return pause(new SystemCall("newTask", coroutine));
    }

    public static <T> Thunk<T> waitTask(Coroutine coroutine, Class<T> retType) {
        return waitTask(coroutine);
    }

    public static <T> Thunk<T> waitTask(Coroutine coroutine) {
        return pause(new SystemCall("waitTask", coroutine));
    }

    public static <T> Thunk<T> waitTid(long tid, Class<T> retType) {
        return waitTid(tid);
    }

    public static <T> Thunk<T> waitTid(long tid) {
        return pause(new SystemCall("waitTid", tid));
    }
}
