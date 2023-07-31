package byx.test.dispatcher;

import byx.test.core.Coroutine;
import byx.test.core.Thunk;

import java.util.function.Consumer;

import static byx.test.core.Thunk.pause;

public interface SystemCall {
    void execute(Task currentTask, Dispatcher dispatcher);

    static SystemCall createTaskCall(Coroutine coroutine) {
        return (currentTask, dispatcher) -> {
            Task newTask = dispatcher.addTask(coroutine);
            currentTask.setSendVal(newTask);
            dispatcher.addToReady(currentTask);
        };
    }

    static SystemCall awaitCall(Coroutine coroutine) {
        return (currentTask, dispatcher) -> {
            Task newTask = dispatcher.addTask(coroutine);
            dispatcher.setTaskWaiting(currentTask, newTask);
        };
    }

    static SystemCall waitCall(Task task) {
        return (currentTask, dispatcher) -> {
            // 如果协程已结束，则无需等待，直接返回
            // 否则将当前协程加入等待队列
            if (dispatcher.isEnd(task)) {
                currentTask.setSendVal(task.getRetVal());
                dispatcher.addToReady(currentTask);
            } else {
                dispatcher.setTaskWaiting(currentTask, task);
            }
        };
    }

    static SystemCall withContinuationCall(Consumer<Continuation> callback) {
        return (currentTask, dispatcher) ->
            callback.accept(value -> {
                currentTask.setSendVal(value);
                dispatcher.addToReady(currentTask);
            });
    }

    static Thunk<Task> createTask(Coroutine coroutine) {
        return pause(createTaskCall(coroutine));
    }

    static <T> Thunk<T> wait(Task task, Class<T> retType) {
        return wait(task);
    }

    static <T> Thunk<T> wait(Task task) {
        return pause(waitCall(task));
    }

    static <T> Thunk<T> await(Coroutine coroutine) {
        return pause(awaitCall(coroutine));
    }

    static <T> Thunk<T> await(Coroutine coroutine, Class<T> retType) {
        return await(coroutine);
    }

    static <T> Thunk<T> withContinuation(Consumer<Continuation> callback) {
        return pause(withContinuationCall(callback));
    }

    static <T> Thunk<T> withContinuation(Consumer<Continuation> callback, Class<T> retType) {
        return withContinuation(callback);
    }
}
