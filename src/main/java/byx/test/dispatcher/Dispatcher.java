package byx.test.dispatcher;

import byx.test.core.Coroutine;
import byx.test.exception.EndOfCoroutineException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class Dispatcher {
    private final BlockingQueue<Task> tasks = new LinkedBlockingQueue<>();
    private final Map<Long, Task> taskMap = new HashMap<>();
    private final Map<Long, List<Task>> waitMap = new HashMap<>();

    public long addTask(Coroutine coroutine) {
        Task task = new Task(coroutine);
        tasks.add(task);
        taskMap.put(task.getTid(), task);
        return task.getTid();
    }

    public void run() {
        while (!taskMap.isEmpty()) {
            Task task;
            try {
                task = tasks.take();
            } catch (InterruptedException e) {
                break;
            }

            try {
                Object ret = task.run();
                if (ret instanceof SystemCall systemCall) {
                    switch (systemCall.getName()) {
                        case SystemCall.AWAIT -> {
                            Coroutine coroutine = (Coroutine) systemCall.getArg();
                            long newTid = addTask(coroutine);
                            waitMap.put(newTid, new ArrayList<>(List.of(task)));
                            continue;
                        }
                        case SystemCall.CREATE_TASK -> {
                            Coroutine coroutine = (Coroutine) systemCall.getArg();
                            long newTid = addTask(coroutine);
                            task.setSendVal(taskMap.get(newTid));
                        }
                        case SystemCall.WAIT -> {
                            Task waitTask = (Task) systemCall.getArg();

                            // 如果协程已结束，则无需等待，直接返回
                            if (!taskMap.containsKey(waitTask.getTid())) {
                                task.setSendVal(waitTask.getRetVal());
                                break;
                            }

                            // 将当前协程加入等待队列
                            waitMap.computeIfAbsent(waitTask.getTid(), t -> new ArrayList<>()).add(task);
                            continue;
                        }
                        case SystemCall.WITH_CONTINUATION -> {
                            Consumer<Continuation> callback = (Consumer<Continuation>) systemCall.getArg();
                            callback.accept(value -> {
                                task.setSendVal(value);
                                tasks.add(task);
                            });
                            continue;
                        }
                        default -> throw new RuntimeException("unknown system call: " + systemCall.getName());
                    }
                }
            } catch (EndOfCoroutineException e) {
                processCoroutineEnd(task, e);
                continue;
            }
            tasks.add(task);
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
