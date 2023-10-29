package byx.trampoline.dispatcher;

import byx.trampoline.core.Coroutine;
import byx.trampoline.exception.EndOfCoroutineException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 协程调度器
 */
public class Dispatcher {
    private final BlockingQueue<Task> tasks = new LinkedBlockingQueue<>();
    private final Map<Long, Task> taskMap = new HashMap<>();
    private final Map<Long, List<Task>> waitMap = new HashMap<>();

    /**
     * 添加任务
     * @param coroutine 协程
     * @return 添加的任务
     */
    public Task addTask(Coroutine coroutine) {
        Task task = new Task(coroutine);
        tasks.add(task);
        taskMap.put(task.getTid(), task);
        return task;
    }

    void addToReady(Task task) {
        tasks.add(task);
    }

    void setTaskWaiting(Task task, Task taskToWait) {
        waitMap.computeIfAbsent(taskToWait.getTid(), t -> new ArrayList<>()).add(task);
    }

    boolean isEnd(Task task) {
        return !taskMap.containsKey(task.getTid());
    }

    /**
     * 运行事件循环
     */
    public void run() {
        while (!taskMap.isEmpty()) {
            // 从就绪队列中取出第一个协程，如果队列为空则阻塞等待
            Task currentTask;
            try {
                currentTask = tasks.take();
            } catch (InterruptedException e) {
                break;
            }

            try {
                // 执行当前协程
                Object ret = currentTask.run();

                // 如果协程返回一个SystemCall，则处理SystemCall
                // 否则将当前协程加入就绪队列末尾
                if (ret instanceof SystemCall systemCall) {
                    systemCall.execute(currentTask, this);
                } else {
                    addToReady(currentTask);
                }
            } catch (EndOfCoroutineException e) {
                // 协程运行结束，执行善后操作
                processCoroutineEnd(currentTask, e);
            }
        }
    }

    private void processCoroutineEnd(Task task, EndOfCoroutineException e) {
        // 设置当前协程返回值
        task.setRetVal(e.getRetVal());

        // 唤醒等待当前协程的协程
        if (waitMap.containsKey(task.getTid())) {
            waitMap.get(task.getTid()).forEach(t -> {
                t.setSendVal(e.getRetVal());
                tasks.add(t);
            });
            waitMap.remove(task.getTid());
        }

        // 移除执行结束的协程
        taskMap.remove(task.getTid());
    }
}
