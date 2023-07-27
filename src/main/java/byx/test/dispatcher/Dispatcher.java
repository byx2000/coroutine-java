package byx.test.dispatcher;

import byx.test.core.Coroutine;
import byx.test.exception.EndOfCoroutineException;

import java.util.*;

public class Dispatcher {
    private final Deque<Task> tasks = new ArrayDeque<>();
    private final Map<Long, Task> taskMap = new HashMap<>();
    private final Map<Long, List<Task>> waitMap = new HashMap<>();

    public long addTask(Coroutine coroutine) {
        Task task = new Task(coroutine);
        tasks.addLast(task);
        taskMap.put(task.getTid(), task);
        return task.getTid();
    }

    public void run() {
        while (!tasks.isEmpty()) {
            Task task = tasks.removeFirst();
            try {
                Object ret = task.run();
                if (ret instanceof SystemCall systemCall) {
                    switch (systemCall.getName()) {
                        case "await" -> {
                            Coroutine coroutine = (Coroutine) systemCall.getArg();
                            long newTid = addTask(coroutine);
                            waitMap.put(newTid, new ArrayList<>(List.of(task)));
                            continue;
                        }
                        case "createTask" -> {
                            Coroutine coroutine = (Coroutine) systemCall.getArg();
                            long newTid = addTask(coroutine);
                            task.setSendVal(taskMap.get(newTid));
                        }
                        case "waitTask" -> {
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
                        default -> throw new RuntimeException("unknown system call: " + systemCall.getName());
                    }
                }
            } catch (EndOfCoroutineException e) {
                // 设置当前协程返回值
                task.setRetVal(e.getRetVal());

                // 唤醒等待当前协程的协程
                if (waitMap.containsKey(task.getTid())) {
                    waitMap.get(task.getTid()).forEach(t -> {
                        t.setSendVal(e.getRetVal());
                        tasks.addLast(t);
                    });
                    waitMap.remove(task.getTid());
                }

                // 如果
                taskMap.remove(task.getTid());
                continue;
            }
            tasks.addLast(task);
        }
    }
}
