package byx.test;

import java.util.*;

public class Dispatcher {
    private final Deque<Task> tasks = new ArrayDeque<>();
    private final Map<Long, Task> waitMap = new HashMap<>();

    public long addTask(Coroutine coroutine) {
        Task task = new Task(coroutine);
        tasks.addLast(task);
        return task.getTid();
    }

    public void run() {
        while (!tasks.isEmpty()) {
            Task task = tasks.removeFirst();
            try {
                Object ret = task.run();
                if (ret instanceof SystemCall systemCall) {
                    switch (systemCall.getName()) {
                        case "getTid" -> task.setSendVal(task.getTid());
                        case "newTask" -> {
                            Coroutine coroutine = (Coroutine) systemCall.getArg();
                            long newTid = addTask(coroutine);
                            task.setSendVal(newTid);
                        }
                        case "waitTask" -> {
                            Coroutine coroutine = (Coroutine) systemCall.getArg();
                            long newTid = addTask(coroutine);
                            waitMap.put(newTid, task);
                            continue;
                        }
                        default -> throw new RuntimeException("unknown system call: " + systemCall.getName());
                    }
                }
            } catch (EndOfCoroutineException e) {
                // 协程运行结束，唤醒等待的协程
                if (waitMap.containsKey(task.getTid())) {
                    Task waittingTask = waitMap.get(task.getTid());
                    waittingTask.setSendVal(e.getReturnValue());
                    tasks.addLast(waittingTask);
                }
                continue;
            }
            tasks.addLast(task);
        }
    }

}
