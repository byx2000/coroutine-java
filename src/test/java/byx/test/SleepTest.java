package byx.test;

import byx.test.core.Coroutine;
import byx.test.dispatcher.Dispatcher;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static byx.test.core.Thunk.exec;
import static byx.test.core.Thunk.value;
import static byx.test.dispatcher.Sleeper.sleep;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SleepTest {
    @Test
    public void testSleep() {
        int count = 100000;
        int maxSleepSecond = 1;
        double t1 = runOnCoroutine(count, maxSleepSecond);
        double t2 = runOnThread(count, maxSleepSecond);
        assertTrue(t1 < t2);
    }

    private static double duration(Runnable runnable) {
        long begin = System.currentTimeMillis();
        runnable.run();
        long end = System.currentTimeMillis();
        return (end - begin) / 1000.0;
    }

    private static double runOnCoroutine(int count, int maxSleepSecond) {
        Dispatcher dispatcher = new Dispatcher();
        Random random = new Random();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        for (int i = 1; i <= count; i++) {
            String name = "co" + i;
            Coroutine co = exec(() -> System.out.printf("[%s] [%-5s] begin\n", format.format(new Date()), name))
                .then(() -> value(random.nextInt(maxSleepSecond * 1000)))
                .flatMap(t -> {
                    System.out.printf("[%s] [%-5s] sleep %ss\n", format.format(new Date()), name, t / 1000.0);
                    return sleep(t, TimeUnit.MILLISECONDS);
                })
                .then(() -> System.out.printf("[%s] [%-5s] end\n", format.format(new Date()), name))
                .toCoroutine();
            dispatcher.addTask(co);
        }

        return duration(dispatcher::run);
    }

    public static double runOnThread(int count, int maxSleepSecond) {
        List<Thread> threads = new ArrayList<>();
        Random random = new Random();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        for (int i = 1; i <= count; i++) {
            String name = "thread-" + i;
            int t = random.nextInt(maxSleepSecond * 1000);
            threads.add(new Thread(() -> {
                System.out.printf("[%s] [%-10s] begin\n", format.format(new Date()), name);
                System.out.printf("[%s] [%-10s] sleep %ss\n", format.format(new Date()), name, t / 1000.0);
                try {
                    TimeUnit.MILLISECONDS.sleep(t);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("[%s] [%-10s] end\n", format.format(new Date()), name);
            }));
        }

        return duration(() -> {
            threads.forEach(Thread::start);
            threads.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
