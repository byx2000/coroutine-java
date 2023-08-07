package byx.test.dispatcher;

import byx.test.core.Coroutine;
import byx.test.core.Thunk;

import java.util.function.Consumer;

import static byx.test.core.Thunk.pause;

/**
 * 协程系统调用
 */
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

    /**
     * 创建新任务
     * @param coroutine 协程
     */
    static Thunk<Task> createTask(Coroutine coroutine) {
        return pause(createTaskCall(coroutine));
    }

    /**
     * 等待指定任务完成
     * @param task 任务
     */
    static <T> Thunk<T> wait(Task task) {
        return pause(waitCall(task));
    }

    /**
     * 等待指定任务完成，并获取返回值
     * @param task 任务
     * @param retType 返回值类型
     */
    static <T> Thunk<T> wait(Task task, Class<T> retType) {
        return wait(task);
    }

    /**
     * 阻塞等待另一个协程完成
     * @param coroutine 协程
     */
    static <T> Thunk<T> await(Coroutine coroutine) {
        return pause(awaitCall(coroutine));
    }

    /**
     * 阻塞等待另一个协程完成，并获取返回值
     * @param coroutine 协程
     * @param retType 返回值类型
     */
    static <T> Thunk<T> await(Coroutine coroutine, Class<T> retType) {
        return await(coroutine);
    }

    /**
     * 捕获当前协程的continuation，并在恰当的时候恢复协程执行
     * @param callback continuation回调
     */
    static <T> Thunk<T> withContinuation(Consumer<Continuation> callback) {
        return pause(withContinuationCall(callback));
    }

    /**
     * 捕获当前协程的continuation，并在恰当的时候恢复协程执行
     * @param callback continuation回调
     * @param retType 返回值类型
     */
    static <T> Thunk<T> withContinuation(Consumer<Continuation> callback, Class<T> retType) {
        return withContinuation(callback);
    }
}
