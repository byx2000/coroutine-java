package byx.trampoline.dispatcher;

import byx.trampoline.core.Coroutine;
import byx.trampoline.core.Trampoline;

import java.util.function.Consumer;

import static byx.trampoline.core.Trampolines.pause;

/**
 * 协程系统调用
 */
public interface SystemCall {
    void execute(Task currentTask, Dispatcher dispatcher);

    /**
     * 创建新任务
     * @param coroutine 协程
     */
    static Trampoline<Task> createTask(Coroutine coroutine) {
        return pause(new CreateTaskCall(coroutine));
    }

    /**
     * 等待指定任务完成
     * @param task 任务
     */
    static <T> Trampoline<T> wait(Task task) {
        return pause(new WaitCall(task));
    }

    /**
     * 等待指定任务完成，并获取返回值
     * @param task 任务
     * @param retType 返回值类型
     */
    static <T> Trampoline<T> wait(Task task, Class<T> retType) {
        return wait(task);
    }

    /**
     * 阻塞等待另一个协程完成
     * @param coroutine 协程
     */
    static <T> Trampoline<T> await(Coroutine coroutine) {
        return pause(new AwaitCall(coroutine));
    }

    /**
     * 阻塞等待另一个协程完成，并获取返回值
     * @param coroutine 协程
     * @param retType 返回值类型
     */
    static <T> Trampoline<T> await(Coroutine coroutine, Class<T> retType) {
        return await(coroutine);
    }

    /**
     * 捕获当前协程的continuation，并在恰当的时候恢复协程执行
     * @param callback continuation回调
     */
    static <T> Trampoline<T> withContinuation(Consumer<Continuation> callback) {
        return pause(new WithContinuationCall(callback));
    }

    /**
     * 捕获当前协程的continuation，并在恰当的时候恢复协程执行
     * @param callback continuation回调
     * @param retType 返回值类型
     */
    static <T> Trampoline<T> withContinuation(Consumer<Continuation> callback, Class<T> retType) {
        return withContinuation(callback);
    }

    class CreateTaskCall implements SystemCall {
        private final Coroutine coroutine;

        public CreateTaskCall(Coroutine coroutine) {
            this.coroutine = coroutine;
        }

        @Override
        public void execute(Task currentTask, Dispatcher dispatcher) {
            Task newTask = dispatcher.addTask(coroutine);
            currentTask.setSendVal(newTask);
            dispatcher.addToReady(currentTask);
        }
    }

    class AwaitCall implements SystemCall {
        private final Coroutine coroutine;

        public AwaitCall(Coroutine coroutine) {
            this.coroutine = coroutine;
        }

        @Override
        public void execute(Task currentTask, Dispatcher dispatcher) {
            Task newTask = dispatcher.addTask(coroutine);
            dispatcher.setTaskWaiting(currentTask, newTask);
        }
    }

    class WaitCall implements SystemCall {
        private final Task task;

        public WaitCall(Task task) {
            this.task = task;
        }

        @Override
        public void execute(Task currentTask, Dispatcher dispatcher) {
            // 如果协程已结束，则无需等待，直接返回
            // 否则将当前协程加入等待队列
            if (dispatcher.isEnd(task)) {
                currentTask.setSendVal(task.getRetVal());
                dispatcher.addToReady(currentTask);
            } else {
                dispatcher.setTaskWaiting(currentTask, task);
            }
        }
    }

    class WithContinuationCall implements SystemCall {
        private final Consumer<Continuation> callback;

        public WithContinuationCall(Consumer<Continuation> callback) {
            this.callback = callback;
        }

        @Override
        public void execute(Task currentTask, Dispatcher dispatcher) {
            callback.accept(value -> {
                currentTask.setSendVal(value);
                dispatcher.addToReady(currentTask);
            });
        }
    }
}
